# ============================================================================
# Скрипт для загрузки данных OSM в PostgreSQL контейнер Docker
# ============================================================================
# Этот скрипт автоматизирует процесс загрузки карточных данных OSM
# в базу данных PostgreSQL, работающую в контейнере Docker.
# ============================================================================

# Включить режим строгой обработки ошибок
$ErrorActionPreference = 'Stop'

# ============================================================================
# Конфигурация
# ============================================================================

$PBF_FILE = "ural-fed-district-260217.osm.pbf"
$CONTAINER_NAME = "uvi-postgres-1"
$POSTGRES_USER = "postgres"
$POSTGRES_PASSWORD = "postgres_secret"
$POSTGRES_DB = "uvi"
$POSTGRES_PORT = "5432"
$POSTGRES_HOST = "localhost"

# Координаты для Екатеринбурга (долгота мин, широта мин, долгота макс, широта макс)
$YEKATERINBURG_BBOX = "60.4,56.6,61.0,57.1"

# ============================================================================
# Вспомогательные функции
# ============================================================================

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Progress {
    param([string]$Message)
    Write-Host "→ $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Exit-OnError {
    param(
        [int]$ExitCode,
        [string]$Message
    )
    if ($ExitCode -ne 0) {
        Write-Error $Message
        exit 1
    }
}

# ============================================================================
# Шаг 1: Проверка наличия PBF файла
# ============================================================================

Write-Host ""
Write-Progress "Шаг 1: Проверка наличия файла OSM (PBF)"
Write-Host "Ищу файл: $PBF_FILE в текущей директории"

if (-not (Test-Path $PBF_FILE)) {
    Write-Error "Файл $PBF_FILE не найден в рабочей директории"
    Write-Host "Текущая директория: $(Get-Location)"
    exit 1
}

$fileSize = (Get-Item $PBF_FILE).Length / 1MB
Write-Success "Файл $PBF_FILE найден (размер: $([math]::Round($fileSize, 2)) МБ)"

# ============================================================================
# Шаг 2: Проверка запуска контейнера PostgreSQL
# ============================================================================

Write-Host ""
Write-Progress "Шаг 2: Проверка запуска контейнера Docker"

$containerStatus = docker ps --filter "name=$CONTAINER_NAME" --format "{{.State}}"
Exit-OnError $LASTEXITCODE "Ошибка при проверке статуса контейнера"

if ([string]::IsNullOrEmpty($containerStatus)) {
    Write-Error "Контейнер $CONTAINER_NAME не запущен"
    Write-Host "Доступные контейнеры:"
    docker ps -a
    exit 1
}

if ($containerStatus -ne "running") {
    Write-Error "Контейнер $CONTAINER_NAME имеет статус: $containerStatus (требуется: running)"
    exit 1
}

Write-Success "Контейнер $CONTAINER_NAME запущен и готов"

# ============================================================================
# Шаг 3: Копирование PBF файла в контейнер
# ============================================================================

Write-Host ""
Write-Progress "Шаг 3: Копирование файла PBF в контейнер"
Write-Host "Копирую $PBF_FILE -> $CONTAINER_NAME:/tmp/map.osm.pbf"

docker cp $PBF_FILE "$CONTAINER_NAME`:/tmp/map.osm.pbf" 2>&1
Exit-OnError $LASTEXITCODE "Ошибка при копировании файла в контейнер"

Write-Success "Файл успешно скопирован в контейнер"

# ============================================================================
# Шаг 4: Установка необходимых утилит в контейнере
# ============================================================================

Write-Host ""
Write-Progress "Шаг 4: Установка необходимых утилит (osm2pgrouting, osmium-tool)"
Write-Host "Обновляю пакеты и устанавливаю зависимости..."

docker exec $CONTAINER_NAME bash -c "apt-get update -qq && apt-get install -y osm2pgrouting osmium-tool" 2>&1
Exit-OnError $LASTEXITCODE "Ошибка при установке утилит в контейнер"

Write-Success "Все необходимые утилиты установлены"

# ============================================================================
# Шаг 5: Извлечение данных Екатеринбурга из PBF
# ============================================================================

Write-Host ""
Write-Progress "Шаг 5: Извлечение данных Екатеринбурга (bbox: $YEKATERINBURG_BBOX)"
Write-Host "Запускаю osmium extract в фоне (занимает ~2-3 минуты)..."

# Удаляем маркеры предыдущих запусков
docker exec $CONTAINER_NAME bash -c "rm -f /tmp/extract_done /tmp/import_done" 2>&1 | Out-Null

# Запускаем в фоне через -d чтобы избежать таймаута Docker
docker exec -d $CONTAINER_NAME bash -c "osmium extract --bbox=$YEKATERINBURG_BBOX /tmp/map.osm.pbf -o /tmp/ekb.osm.pbf --overwrite && touch /tmp/extract_done"
Exit-OnError $LASTEXITCODE "Ошибка при запуске osmium extract"

# Ожидаем завершения (проверяем каждые 15 секунд, максимум 5 минут)
$maxWait = 300
$waited = 0
while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 15
    $waited += 15
    $done = docker exec $CONTAINER_NAME bash -c "test -f /tmp/extract_done && echo yes || echo no" 2>&1
    if ($done.Trim() -eq "yes") { break }
    Write-Host "  Прошло $waited сек., ожидаем завершения osmium extract..." -ForegroundColor DarkGray
}
if ($waited -ge $maxWait) {
    Write-Error "Таймаут: osmium extract не завершился за $maxWait секунд"
    exit 1
}

$ekbSize = docker exec $CONTAINER_NAME bash -c "du -sh /tmp/ekb.osm.pbf | cut -f1" 2>&1
Write-Success "Данные Екатеринбурга извлечены в /tmp/ekb.osm.pbf ($($ekbSize.Trim()))"

# ============================================================================
# Шаг 6: Конвертация PBF в OSM XML и загрузка в PostgreSQL
# ============================================================================

Write-Host ""
Write-Progress "Шаг 6: Конвертация PBF → OSM XML и загрузка в PostgreSQL"
Write-Host "Запускаю osmium cat + osm2pgrouting в фоне (занимает ~5-7 минут)..."

$importCmd = "osmium cat /tmp/ekb.osm.pbf -o /tmp/ekb.osm --overwrite && " +
    "PGPASSWORD=$POSTGRES_PASSWORD osm2pgrouting " +
    "--file /tmp/ekb.osm " +
    "--host $POSTGRES_HOST " +
    "--port $POSTGRES_PORT " +
    "--dbname $POSTGRES_DB " +
    "--username $POSTGRES_USER " +
    "--clean && touch /tmp/import_done"

docker exec -d $CONTAINER_NAME bash -c $importCmd
Exit-OnError $LASTEXITCODE "Ошибка при запуске импорта"

# Ожидаем завершения (проверяем каждые 30 секунд, максимум 15 минут)
$maxWait = 900
$waited = 0
while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 30
    $waited += 30
    $done = docker exec $CONTAINER_NAME bash -c "test -f /tmp/import_done && echo yes || echo no" 2>&1
    if ($done.Trim() -eq "yes") { break }
    Write-Host "  Прошло $waited сек., ожидаем завершения импорта..." -ForegroundColor DarkGray
}
if ($waited -ge $maxWait) {
    Write-Error "Таймаут: импорт не завершился за $maxWait секунд"
    exit 1
}

Write-Success "Данные успешно конвертированы и загружены в PostgreSQL"

# ============================================================================
# Шаг 7: Создание дополнительных индексов
# ============================================================================

Write-Host ""
Write-Progress "Шаг 7: Создание дополнительных индексов для таблицы ways"

$createIndexesCmd = "PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c " +
    "'CREATE INDEX IF NOT EXISTS ways_source_idx ON ways(source); " +
    "CREATE INDEX IF NOT EXISTS ways_target_idx ON ways(target);'"

docker exec $CONTAINER_NAME bash -c $createIndexesCmd 2>&1
Exit-OnError $LASTEXITCODE "Ошибка при создании индексов"

Write-Success "Индексы успешно созданы"

# ============================================================================
# Шаг 8: Проверка загруженных данных
# ============================================================================

Write-Host ""
Write-Progress "Шаг 8: Проверка загруженных данных"

# Подсчет строк в таблице ways
Write-Host "Подсчет строк в таблице ways..."
$waysCountCmd = "PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -t -c 'SELECT COUNT(1) FROM ways;'"
$waysCount = docker exec $CONTAINER_NAME bash -c $waysCountCmd 2>&1
Exit-OnError $LASTEXITCODE "Ошибка при подсчете строк в ways"
$waysCount = $waysCount.Trim()

# Подсчет вершин
Write-Host "Подсчет вершин в таблице ways_vertices_pgr..."
$verticesCountCmd = "PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -t -c 'SELECT COUNT(1) FROM ways_vertices_pgr;'"
$verticesCount = docker exec $CONTAINER_NAME bash -c $verticesCountCmd 2>&1
Exit-OnError $LASTEXITCODE "Ошибка при подсчете вершин"
$verticesCount = $verticesCount.Trim()

Write-Success "Проверка завершена:"
Write-Host "  - Строк в таблице ways: $waysCount" -ForegroundColor Cyan
Write-Host "  - Вершин в таблице ways_vertices_pgr: $verticesCount" -ForegroundColor Cyan

# ============================================================================
# Шаг 9: Очистка временных файлов в контейнере
# ============================================================================

Write-Host ""
Write-Progress "Шаг 9: Очистка временных файлов в контейнере"
Write-Host "Удаляю временные файлы..."

docker exec $CONTAINER_NAME bash -c "rm -f /tmp/map.osm.pbf /tmp/ekb.osm.pbf /tmp/ekb.osm" 2>&1
Exit-OnError $LASTEXITCODE "Ошибка при удалении временных файлов"

Write-Success "Временные файлы удалены"

# ============================================================================
# Завершение
# ============================================================================

Write-Host ""
Write-Host "=" * 80
Write-Success "Загрузка OSM данных в PostgreSQL успешно завершена!"
Write-Host "=" * 80
Write-Host ""
Write-Host "Статистика загрузки:" -ForegroundColor Cyan
Write-Host "  - Файл PBF: $PBF_FILE ($([math]::Round($fileSize, 2)) МБ)" -ForegroundColor Cyan
Write-Host "  - Контейнер: $CONTAINER_NAME" -ForegroundColor Cyan
Write-Host "  - База данных: $POSTGRES_DB" -ForegroundColor Cyan
Write-Host "  - Загруженные пути (ways): $waysCount" -ForegroundColor Cyan
Write-Host "  - Вершины маршрутизации: $verticesCount" -ForegroundColor Cyan
Write-Host ""
