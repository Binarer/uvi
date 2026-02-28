// ===== MAP =====
const map = L.map('map', { zoomControl: true }).setView([56.8389, 60.6057], 13);
L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> © <a href="https://carto.com/">CARTO</a>',
    subdomains: 'abcd', maxZoom: 19
}).addTo(map);

// ===== STATE =====
let jwtToken       = localStorage.getItem('uvi_jwt') || '';
let currentUserId  = null;
let clickMode      = 'start';
let routeMode      = 'DRIVING';
let startMarker    = null;
let endMarker      = null;
let myLocMarker    = null;   // маркер моей геопозиции
let routeLayer     = null;
let animLayer      = null;
let mqttClient     = null;
let mqttTracking   = false;
let geoWatchId     = null;

// GPS noise filtering
let lastPublishedLat = null;
let lastPublishedLon = null;
let lastPublishedAccuracy = null;

// ===== ICONS =====
const startIcon = L.divIcon({ className: '', html: `<div style="width:18px;height:18px;background:#fff;border:3px solid #222;border-radius:50%;box-shadow:0 2px 8px rgba(0,0,0,0.6)"></div>`, iconAnchor: [9,9] });
const endIcon   = L.divIcon({ className: '', html: `<div style="width:18px;height:18px;background:#222;border:3px solid #fff;border-radius:50%;box-shadow:0 2px 8px rgba(0,0,0,0.8)"></div>`, iconAnchor: [9,9] });
const myIcon    = L.divIcon({ className: '', html: `<div style="width:20px;height:20px;background:#4a9;border:3px solid #fff;border-radius:50%;box-shadow:0 0 0 4px rgba(68,170,100,0.25)"></div>`, iconAnchor: [10,10] });

// ===== ROUTE STYLES =====
const routeStyles = {
    DRIVING:          { bgColor:'#999', bgWeight:2, bgOpacity:0.35, dashColor:'#111', dashWeight:5, dashArray:'14 10', dashSpeed:'0.8s', label:'🚗 Авто' },
    PEDESTRIAN:       { bgColor:'#aaa', bgWeight:1.5, bgOpacity:0.25, dashColor:'#333', dashWeight:3, dashArray:'4 8', dashSpeed:'1.2s', label:'🚶 Пешком' },
    PUBLIC_TRANSPORT: { bgColor:'#bbb', bgWeight:2.5, bgOpacity:0.3, dashColor:'#111', dashWeight:6, dashArray:'18 8', dashSpeed:'0.6s', label:'🚌 Транспорт' }
};

// ===== AUTH: restore from localStorage =====
if (jwtToken) {
    try {
        const payload = JSON.parse(atob(jwtToken.split('.')[1]));
        currentUserId = payload.sub;
        setAuthStatus(true, payload.phone || '');
    } catch(e) { jwtToken = ''; localStorage.removeItem('uvi_jwt'); }
}

function setAuthStatus(ok, phone) {
    const txt = document.getElementById('authStatusText');
    const btn = document.querySelector('.btn-auth');
    if (ok) {
        txt.textContent = '✓ ' + (phone || 'авторизован');
        txt.className = 'auth-status ok';
        btn.textContent = '🔓 Выйти';
        btn.onclick = logout;
    } else {
        txt.textContent = 'Не авторизован';
        txt.className = 'auth-status';
        btn.textContent = '🔐 Войти';
        btn.onclick = openModal;
    }
}

async function logout() {
    try {
        await fetch('/api/v1/auth/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + jwtToken }
        });
    } catch(e) {
        // Logout still continues even if API call fails
    }
    jwtToken = ''; currentUserId = null;
    localStorage.removeItem('uvi_jwt');
    localStorage.removeItem('uvi_refresh_token');
    setAuthStatus(false, '');
    stopMqtt();
}

// ===== AUTH MODAL =====
function openModal() { document.getElementById('authModal').classList.add('open'); showStep('phone'); }
function closeModal() { document.getElementById('authModal').classList.remove('open'); }

function showStep(name) {
    document.querySelectorAll('.modal-step').forEach(s => s.classList.remove('active'));
    document.getElementById('step-' + name).classList.add('active');
    document.querySelectorAll('.modal-error').forEach(e => { e.style.display='none'; e.textContent=''; });
}

function showModalError(id, msg) {
    const el = document.getElementById(id);
    el.textContent = msg; el.style.display = 'block';
}

async function sendCode(check2fa = false) {
    const phone = document.getElementById('authPhone').value.trim();
    if (!/^[78]\d{10}$/.test(phone)) { showModalError('phoneError', 'Формат: 79XXXXXXXXX (11 цифр)'); return; }
    const btn = document.getElementById('btnSendCode');
    btn.disabled = true; btn.textContent = '⏳ Отправка...';
    try {
        const r = await fetch('/api/v1/auth/send-code', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phoneNumber: phone })
        });
        if (!r.ok) { const d = await r.json(); throw new Error(d.message || 'Ошибка ' + r.status); }
        showStep('code');
        document.getElementById('authCode').focus();
    } catch(e) { showModalError('phoneError', e.message); }
    finally { btn.disabled = false; btn.textContent = 'Отправить код →'; }
}

async function verifyCode() {
    const phone = document.getElementById('authPhone').value.trim();
    const code  = document.getElementById('authCode').value.trim();
    if (!/^\d{6}$/.test(code)) { showModalError('codeError', 'Код — 6 цифр'); return; }
    const btn = document.getElementById('btnVerify');
    btn.disabled = true; btn.textContent = '⏳ Проверка...';
    try {
        const r = await fetch('/api/v1/auth/verify-code', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phoneNumber: phone, code })
        });
        const d = await r.json();
        if (!r.ok) throw new Error(d.message || 'Ошибка ' + r.status);
        
        // Handle 2FA requirement
        if (d.twoFactorRequired === true) {
            jwtToken = d.twoFactorTempToken;
            showStep('2fa');
            document.getElementById('authTotpCode').focus();
            return;
        }
        
        // Standard auth flow
        jwtToken = d.accessToken;
        if (d.refreshToken) {
            localStorage.setItem('uvi_refresh_token', d.refreshToken);
        }
        localStorage.setItem('uvi_jwt', jwtToken);
        const payload = JSON.parse(atob(jwtToken.split('.')[1]));
        currentUserId = payload.sub;
        setAuthStatus(true, phone);
        document.getElementById('successPhone').textContent = phone;
        if (d.expiresIn) {
            scheduleTokenRefresh(d.expiresIn);
        }
        showStep('success');
    } catch(e) { showModalError('codeError', e.message); }
    finally { btn.disabled = false; btn.textContent = 'Войти ✓'; }
}

async function verify2fa() {
    const totpCode = document.getElementById('authTotpCode').value.trim();
    if (!/^\d{6}$/.test(totpCode)) { showModalError('totpError', 'Код — 6 цифр'); return; }
    const btn = document.getElementById('btnVerify2fa');
    btn.disabled = true; btn.textContent = '⏳ Проверка...';
    try {
        const r = await fetch('/api/v1/auth/2fa/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + jwtToken },
            body: JSON.stringify({ code: parseInt(totpCode) })
        });
        const d = await r.json();
        if (!r.ok) throw new Error(d.message || 'Ошибка ' + r.status);
        
        jwtToken = d.accessToken;
        if (d.refreshToken) {
            localStorage.setItem('uvi_refresh_token', d.refreshToken);
        }
        localStorage.setItem('uvi_jwt', jwtToken);
        const payload = JSON.parse(atob(jwtToken.split('.')[1]));
        currentUserId = payload.sub;
        setAuthStatus(true, '');
        if (d.expiresIn) {
            scheduleTokenRefresh(d.expiresIn);
        }
        showStep('success');
    } catch(e) { showModalError('totpError', e.message); }
    finally { btn.disabled = false; btn.textContent = 'Подтвердить ✓'; }
}

function scheduleTokenRefresh(expiresInSeconds) {
    const refreshTime = (expiresInSeconds * 0.8) * 1000;
    setTimeout(refreshAccessToken, refreshTime);
}

async function refreshAccessToken() {
    const refreshToken = localStorage.getItem('uvi_refresh_token');
    if (!refreshToken) {
        openModal();
        return;
    }
    try {
        const r = await fetch('/api/v1/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });
        const d = await r.json();
        if (!r.ok) throw new Error(d.message || 'Ошибка ' + r.status);
        
        jwtToken = d.accessToken;
        if (d.refreshToken) {
            localStorage.setItem('uvi_refresh_token', d.refreshToken);
        }
        localStorage.setItem('uvi_jwt', jwtToken);
        if (d.expiresIn) {
            scheduleTokenRefresh(d.expiresIn);
        }
    } catch(e) {
        localStorage.removeItem('uvi_jwt');
        localStorage.removeItem('uvi_refresh_token');
        jwtToken = '';
        currentUserId = null;
        setAuthStatus(false, '');
        openModal();
    }
}

function backToPhone() { showStep('phone'); }

// Enter в модалке
document.getElementById('authPhone').addEventListener('keydown', e => { if (e.key === 'Enter') sendCode(); });
document.getElementById('authCode').addEventListener('keydown',  e => { if (e.key === 'Enter') verifyCode(); });
document.getElementById('authModal').addEventListener('click', e => { if (e.target === document.getElementById('authModal')) closeModal(); });

// 2FA modal handler (will be added to DOM via HTML update)
document.addEventListener('DOMContentLoaded', () => {
    const totpInput = document.getElementById('authTotpCode');
    if (totpInput) {
        totpInput.addEventListener('keydown', e => { if (e.key === 'Enter') verify2fa(); });
    }
});

// ===== CLICK MODE =====
function setClickMode(m) {
    clickMode = m;
    document.getElementById('btn-mode-start').className = 'mode-btn' + (m==='start' ? ' active-start' : '');
    document.getElementById('btn-mode-end').className   = 'mode-btn' + (m==='end'   ? ' active-end'   : '');
}

map.on('click', function(e) {
    const lat = e.latlng.lat.toFixed(6), lon = e.latlng.lng.toFixed(6);
    if (clickMode === 'start') {
        document.getElementById('startLat').value = lat;
        document.getElementById('startLon').value = lon;
        if (startMarker) map.removeLayer(startMarker);
        startMarker = L.marker([lat,lon], {icon:startIcon}).addTo(map).bindPopup(`<b>Начало</b><br>${lat}, ${lon}`).openPopup();
        setClickMode('end');
    } else {
        document.getElementById('endLat').value = lat;
        document.getElementById('endLon').value = lon;
        if (endMarker) map.removeLayer(endMarker);
        endMarker = L.marker([lat,lon], {icon:endIcon}).addTo(map).bindPopup(`<b>Конец</b><br>${lat}, ${lon}`).openPopup();
        setClickMode('start');
    }
});

// ===== ROUTE MODE =====
function selectRouteMode(m) {
    routeMode = m;
    document.querySelectorAll('.route-mode-btn').forEach(b => b.classList.remove('active'));
    document.getElementById('mode-' + m).classList.add('active');
}

// ===== CALCULATE ROUTE =====
async function calculateRoute() {
    const startLat = parseFloat(document.getElementById('startLat').value);
    const startLon = parseFloat(document.getElementById('startLon').value);
    const endLat   = parseFloat(document.getElementById('endLat').value);
    const endLon   = parseFloat(document.getElementById('endLon').value);
    if (isNaN(startLat)||isNaN(startLon)||isNaN(endLat)||isNaN(endLon)) { showError('Заполните координаты'); return; }
    if (!jwtToken) { showError('Необходима авторизация'); openModal(); return; }
    document.getElementById('calcBtn').disabled = true;
    document.getElementById('spinner').style.display = 'block';
    document.getElementById('resultCard').style.display = 'none';
    try {
        const r = await fetch('/api/v1/routes/calculate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + jwtToken },
            body: JSON.stringify({ startLat, startLon, endLat, endLon, mode: routeMode })
        });
        const data = await r.json();
        if (!r.ok) { showError(data.message || 'Ошибка ' + r.status); return; }
        showRoute(data);
    } catch(e) { showError('Ошибка: ' + e.message); }
    finally { document.getElementById('calcBtn').disabled = false; document.getElementById('spinner').style.display = 'none'; }
}

// ===== ROUTE DISPLAY =====
function clearRouteLayer() {
    if (routeLayer) { map.removeLayer(routeLayer); routeLayer = null; }
    if (animLayer)  { map.removeLayer(animLayer);  animLayer  = null; }
}

function showRoute(data) {
    clearRouteLayer();
    const latlngs = data.points.map(p => [p.latitude, p.longitude]);
    const style   = routeStyles[data.mode] || routeStyles['DRIVING'];
    routeLayer = L.polyline(latlngs, { color:style.bgColor, weight:style.bgWeight, opacity:style.bgOpacity, lineJoin:'round', lineCap:'round' }).addTo(map);
    animLayer  = L.polyline(latlngs, { color:style.dashColor, weight:style.dashWeight, opacity:1, lineJoin:'round', lineCap:'butt', dashArray:style.dashArray, dashOffset:'0' }).addTo(map);
    requestAnimationFrame(() => {
        const paths = document.querySelectorAll('.leaflet-overlay-pane svg path');
        if (paths.length > 0) {
            const p = paths[paths.length - 1];
            p.style.strokeDasharray = style.dashArray;
            p.style.strokeDashoffset = '0';
            p.style.animation = `dash-flow ${style.dashSpeed} linear infinite`;
        }
    });
    map.fitBounds(routeLayer.getBounds(), { padding: [50,50] });
    if (startMarker) map.removeLayer(startMarker);
    if (endMarker)   map.removeLayer(endMarker);
    startMarker = L.marker([data.startLat, data.startLon], {icon:startIcon}).addTo(map).bindPopup(`<b>Начало</b><br>${data.startLat.toFixed(5)}, ${data.startLon.toFixed(5)}`);
    endMarker   = L.marker([data.endLat,   data.endLon],   {icon:endIcon  }).addTo(map).bindPopup(`<b>Конец</b><br>${data.endLat.toFixed(5)}, ${data.endLon.toFixed(5)}`);
    const card = document.getElementById('resultCard');
    card.className = 'result-card'; card.style.display = 'block';
    document.getElementById('resultTitle').textContent  = `✓ Маршрут построен (${style.label})`;
    document.getElementById('resDist').textContent      = `${formatDist(data.totalDistanceMeters)} / ${data.totalDistanceKm} км`;
    document.getElementById('resPoints').textContent    = data.totalSegments;
    document.getElementById('resStart').textContent     = `${data.startLat.toFixed(4)}, ${data.startLon.toFixed(4)}`;
    document.getElementById('resEnd').textContent       = `${data.endLat.toFixed(4)},   ${data.endLon.toFixed(4)}`;
}

function showError(msg) {
    document.getElementById('spinner').style.display = 'none';
    document.getElementById('calcBtn').disabled = false;
    const card = document.getElementById('resultCard');
    card.className = 'result-card error'; card.style.display = 'block';
    document.getElementById('resultTitle').textContent = '✕ ' + msg;
    ['resDist','resPoints','resStart','resEnd'].forEach(id => document.getElementById(id).textContent = '—');
}

function formatDist(m) { return m >= 1000 ? (m/1000).toFixed(2)+' км' : Math.round(m)+' м'; }

function clearAll() {
    clearRouteLayer();
    if (startMarker) { map.removeLayer(startMarker); startMarker = null; }
    if (endMarker)   { map.removeLayer(endMarker);   endMarker   = null; }
    ['startLat','startLon','endLat','endLon'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('resultCard').style.display = 'none';
    setClickMode('start');
}

// ===== MQTT GEOLOCATION =====
function setMqttStatus(state, text) {
    const dot = document.getElementById('mqttDot');
    dot.className = 'mqtt-dot' + (state === 'ok' ? ' connected' : state === 'err' ? ' error' : '');
    document.getElementById('mqttStatusText').textContent = text;
}

function toggleMqtt() {
    if (mqttTracking) stopMqtt(); else startMqtt();
}

function startMqtt() {
    if (!jwtToken) { alert('Для MQTT трекинга необходима авторизация'); openModal(); return; }
    if (!navigator.geolocation) { setMqttStatus('err', 'Геолокация не поддерживается'); return; }

    // Подключаемся к Mosquitto WebSocket на порту 9001
    const clientId = 'uvi-web-' + Math.random().toString(36).substr(2, 8);
    mqttClient = mqtt.connect('ws://localhost:9001', { clientId, keepalive: 30 });

    mqttClient.on('connect', () => {
        setMqttStatus('ok', 'MQTT подключён');
        mqttTracking = true;
        document.getElementById('mqttBtnText').textContent = '■ Остановить трекинг';

        // Начинаем слежение за геолокацией
        geoWatchId = navigator.geolocation.watchPosition(
            pos => publishLocation(pos),
            err => setMqttStatus('err', 'Ошибка геолокации: ' + err.message),
            { enableHighAccuracy: true, maximumAge: 5000, timeout: 10000 }
        );
    });

    mqttClient.on('error', err => {
        setMqttStatus('err', 'Ошибка MQTT: ' + err.message);
        stopMqtt();
    });

    mqttClient.on('close', () => {
        if (mqttTracking) setMqttStatus('', 'MQTT отключён');
    });
}

function publishLocation(pos) {
    if (!mqttClient || !mqttClient.connected || !currentUserId) return;
    const { latitude, longitude, accuracy, speed } = pos.coords;
    
    // GPS noise filtering: accuracy check (50m threshold)
    if (accuracy && accuracy > 50) {
        document.getElementById('mqttCoords').textContent = `⚠️ GPS accuracy too low: ±${Math.round(accuracy)}м (max: 50м)`;
        return;
    }
    
    // GPS noise filtering: distance check (5m threshold)
    if (lastPublishedLat !== null && lastPublishedLon !== null) {
        const distance = euclideanDistance(lastPublishedLat, lastPublishedLon, latitude, longitude);
        if (distance < 5) {
            // Skip publishing, but still show current coordinates
            document.getElementById('mqttCoords').textContent =
                `${latitude.toFixed(6)}, ${longitude.toFixed(6)}` +
                (accuracy ? ` ±${Math.round(accuracy)}м` : '') + ' (filtered)';
            return;
        }
    }
    
    // Update last published position
    lastPublishedLat = latitude;
    lastPublishedLon = longitude;
    lastPublishedAccuracy = accuracy;
    
    const msg = JSON.stringify({
        userId:    parseInt(currentUserId),
        latitude,
        longitude,
        accuracy:  accuracy  || null,
        speed:     speed     || null,
        batteryLevel: null,
        timestamp: Date.now()
    });
    const topic = 'location/' + currentUserId;
    mqttClient.publish(topic, msg, { qos: 1 });

    // Обновляем маркер на карте
    if (!myLocMarker) {
        myLocMarker = L.marker([latitude, longitude], { icon: myIcon })
            .addTo(map)
            .bindPopup('<b>📍 Я</b>');
    } else {
        myLocMarker.setLatLng([latitude, longitude]);
    }

    // Обновляем координаты в сайдбаре
    document.getElementById('mqttCoords').textContent =
        `${latitude.toFixed(6)}, ${longitude.toFixed(6)}` +
        (accuracy ? ` ±${Math.round(accuracy)}м` : '');
}

function euclideanDistance(lat1, lon1, lat2, lon2) {
    // Simple Euclidean approximation for small distances
    const deltaLat = lat2 - lat1;
    const deltaLon = (lon2 - lon1) * Math.cos((lat1 + lat2) / 2 * Math.PI / 180);
    const meters = Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon) * 111320;
    return meters;
}

function stopMqtt() {
    mqttTracking = false;
    if (geoWatchId !== null) { navigator.geolocation.clearWatch(geoWatchId); geoWatchId = null; }
    if (mqttClient) { mqttClient.end(true); mqttClient = null; }
    setMqttStatus('', 'Не подключён');
    document.getElementById('mqttBtnText').textContent = '▶ Начать трекинг';
    document.getElementById('mqttCoords').textContent = '';
}

// ===== KEYBOARD =====
document.addEventListener('keydown', e => { if (e.key === 'Enter' && !document.getElementById('authModal').classList.contains('open')) calculateRoute(); });

// ===== INIT MARKERS =====
startMarker = L.marker([56.8376, 60.6425], {icon:startIcon}).addTo(map).bindPopup('<b>Начало</b> — Площадь 1905 года');
endMarker   = L.marker([56.8368, 60.6146], {icon:endIcon  }).addTo(map).bindPopup('<b>Конец</b> — ЖД вокзал');

// Если уже авторизован — показываем статус
if (!jwtToken) setTimeout(openModal, 500);
