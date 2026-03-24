import psycopg2
import os
from datetime import datetime

# Database connection parameters
DB_HOST = "localhost"
DB_PORT = "5432"
DB_NAME = "uvi"
DB_USER = "postgres"
DB_PASS = "postgres_secret"

def populate_places():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
        cur = conn.cursor()

        places = [
            # --- MUSEUMS & CULTURE ---
            {"name": "Ельцин Центр", "type": "MUSEUM", "address": "ул. Бориса Ельцина, 3", "lat": 56.8447, "lon": 60.5908, "description": "Культурно-образовательный центр."},
            {"name": "Музей Высоцкого", "type": "MUSEUM", "address": "ул. Малышева, 51", "lat": 56.8361, "lon": 60.6146, "description": "Музей памяти Владимира Высоцкого."},
            {"name": "Музей ИЗО (Воеводина)", "type": "MUSEUM", "address": "ул. Воеводина, 5", "lat": 56.8365, "lon": 60.6053, "description": "Каслинское литье и русская живопись."},
            {"name": "Музей истории Екатеринбурга", "type": "MUSEUM", "address": "ул. Карла Либкнехта, 26", "lat": 56.8405, "lon": 60.6105, "description": "История города от основания до наших дней."},
            {"name": "Музей природы", "type": "MUSEUM", "address": "ул. Горького, 4", "lat": 56.8385, "lon": 60.6058, "description": "Флора и фауна Урала."},
            {"name": "Музей архитектуры и дизайна", "type": "MUSEUM", "address": "ул. Горького, 4А", "lat": 56.8392, "lon": 60.6062, "description": "Индустриальное наследие Урала."},
            {"name": "Екатеринбургский цирк", "type": "THEATER", "address": "ул. 8 Марта, 43", "lat": 56.8256, "lon": 60.6061, "description": "Цирк с уникальным куполом."},
            {"name": "Оперный театр", "type": "THEATER", "address": "пр. Ленина, 46", "lat": 56.8389, "lon": 60.6171, "description": "Театр оперы и балета."},
            {"name": "Театр Драмы", "type": "THEATER", "address": "Октябрьская пл., 2", "lat": 56.8435, "lon": 60.5915, "description": "Главный драматический театр города."},
            {"name": "Коляда-Театр", "type": "THEATER", "address": "пр. Ленина, 97", "lat": 56.8401, "lon": 60.6255, "description": "Знаменитый частный театр Николая Коляды."},

            # --- SIGHTS & LANDMARKS ---
            {"name": "Храм на Крови", "type": "SIGHT", "address": "ул. Царская, 10", "lat": 56.8444, "lon": 60.6085, "description": "Храм на месте дома Ипатьева."},
            {"name": "Дом Севастьянова", "type": "SIGHT", "address": "пр. Ленина, 35", "lat": 56.8394, "lon": 60.6067, "description": "Дворец в стиле неомавританской готики."},
            {"name": "Памятник Татищеву и де Геннину", "type": "SIGHT", "address": "Площадь Труда", "lat": 56.8391, "lon": 60.6075, "description": "Памятник основателям города."},
            {"name": "Памятник Клавиатуре", "type": "SIGHT", "address": "ул. Горького", "lat": 56.8325, "lon": 60.6072, "description": "Бетонная копия компьютерной клавиатуры."},
            {"name": "Белая башня", "type": "SIGHT", "address": "ул. Бакинских Комиссаров, 2А", "lat": 56.8901, "lon": 60.5841, "description": "Шедевр конструктивизма."},
            {"name": "Городок Чекистов", "type": "SIGHT", "address": "пр. Ленина, 69", "lat": 56.8415, "lon": 60.6205, "description": "Памятник конструктивизма."},
            {"name": "Штаб Центрального военного округа", "type": "SIGHT", "address": "пр. Ленина, 71", "lat": 56.8421, "lon": 60.6241, "description": "Монументальное сталинское здание."},

            # --- PARKS & OUTDOOR ---
            {"name": "ЦПКиО им. Маяковского", "type": "PARK", "address": "ул. Мичурина, 230", "lat": 56.8125, "lon": 60.6405, "description": "Главный парк отдыха и аттракционов."},
            {"name": "Харитоновский сад", "type": "PARK", "address": "ул. Клары Цеткин, 11", "lat": 56.8465, "lon": 60.6111, "description": "Старинный парк с ротондой."},
            {"name": "Дендропарк (8 Марта)", "type": "PARK", "address": "ул. 8 Марта, 37А", "lat": 56.8285, "lon": 60.6045, "description": "Коллекция растений в центре города."},
            {"name": "Парк Зеленая Роща", "type": "PARK", "address": "ул. Шейнкмана", "lat": 56.8225, "lon": 60.5955, "description": "Лесной массив в центре города."},
            {"name": "Метеогорка", "type": "PARK", "address": "ул. Бажова", "lat": 56.8265, "lon": 60.6235, "description": "Смотровая площадка и парк."},
            {"name": "Сад им. Энгельса", "type": "PARK", "address": "ул. Малышева/Бажова", "lat": 56.8345, "lon": 60.6225, "description": "Небольшой уютный сад."},
            {"name": "Шарташские каменные палатки", "type": "PARK", "address": "ул. Высоцкого", "lat": 56.8455, "lon": 60.6725, "description": "Природный памятник и парк отдыха."},

            # --- RESTAURANTS & BARS ---
            {"name": "Гастроли", "type": "RESTAURANT", "address": "ул. 8 Марта, 8Б", "lat": 56.8375, "lon": 60.5985, "description": "Популярный ресторан с авторской кухней."},
            {"name": "Momo pan-asian kitchen", "type": "RESTAURANT", "address": "ул. Карла Либкнехта, 38А", "lat": 56.8412, "lon": 60.6125, "description": "Паназиатская кухня."},
            {"name": "Kitchen", "type": "RESTAURANT", "address": "ул. Ткачей, 23", "lat": 56.8145, "lon": 60.6355, "description": "Ресторан на 23 этаже с видом на парк."},
            {"name": "Pashtet", "type": "RESTAURANT", "address": "ул. Толмачева, 23", "lat": 56.8395, "lon": 60.6095, "description": "Домашняя кухня и уютная атмосфера."},
            {"name": "Barbara", "type": "RESTAURANT", "address": "ул. Сакко и Ванцетти, 99", "lat": 56.8315, "lon": 60.5925, "description": "Стильный ресторан с морепродуктами."},
            {"name": "Grott Bar", "type": "BAR", "address": "пр. Ленина, 49", "lat": 56.8398, "lon": 60.6145, "description": "Скандинавский паб и пивоварня."},
            {"name": "Jawsspot", "type": "BAR", "address": "ул. Тургенева, 3", "lat": 56.8418, "lon": 60.6115, "description": "Культовый крафтовый бар."},
            {"name": "InTouch Cocktail Bar", "type": "BAR", "address": "ул. Розы Люксембург, 23", "lat": 56.8335, "lon": 60.6105, "description": "Коктейльный бар с живой музыкой."},
            {"name": "Double Bar", "type": "BAR", "address": "ул. 8 Марта, 8Б", "lat": 56.8372, "lon": 60.5982, "description": "Уютный бар с отличными напитками."},
            {"name": "Engels Coffee", "type": "CAFE", "address": "ул. Малышева, 21/4", "lat": 56.8355, "lon": 60.5945, "description": "Лучшие вафли и кофе в городе."},
            {"name": "Simple Coffee", "type": "CAFE", "address": "пр. Ленина, 46", "lat": 56.8385, "lon": 60.6175, "description": "Популярная сеть городских кофеен."},

            # --- SHOPPING & LEISURE ---
            {"name": "ТРЦ Гринвич", "type": "SHOPPING", "address": "ул. 8 Марта, 46", "lat": 56.8297, "lon": 60.5972, "description": "Крупнейший ТРЦ города."},
            {"name": "ТЦ Пассаж", "type": "SHOPPING", "address": "ул. Вайнера, 9", "lat": 56.8375, "lon": 60.5985, "description": "Торговый центр с кинотеатром."},
            {"name": "ТЦ Европа", "type": "SHOPPING", "address": "пр. Ленина, 25", "lat": 56.8385, "lon": 60.5955, "description": "Элитный торговый центр."},
            {"name": "Универмаг Bolshoy", "type": "SHOPPING", "address": "ул. Малышева, 71", "lat": 56.8355, "lon": 60.6185, "description": "Концепт-стор и фуд-маркет."},
            {"name": "Екатеринбург Арена", "type": "SPORTS", "address": "ул. Репина, 5", "lat": 56.8325, "lon": 60.5735, "description": "Стадион чемпионата мира 2018."},
            {"name": "КРК Уралец", "type": "SPORTS", "address": "ул. Большакова, 90", "lat": 56.8215, "lon": 60.5995, "description": "Хоккейная арена клуба Автомобилист."},

            # --- STREETS & SQUARES ---
            {"name": "Площадь 1905 года", "type": "SQUARE", "address": "Площадь 1905 года", "lat": 56.8381, "lon": 60.5961, "description": "Главная площадь города."},
            {"name": "Улица Вайнера", "type": "STREET", "address": "ул. Вайнера", "lat": 56.8341, "lon": 60.5986, "description": "Пешеходный Арбат."},
            {"name": "Литературный квартал", "type": "SIGHT", "address": "ул. Пролетарская", "lat": 56.8425, "lon": 60.6085, "description": "Уникальный музейный комплекс под открытым небом."},
            {"name": "Набережная рабочей молодежи", "type": "STREET", "address": "Набережная рабочей молодежи", "lat": 56.8415, "lon": 60.5955, "description": "Красивая набережная у Исети."},
            {"name": "Октябрьская площадь", "type": "SQUARE", "address": "Октябрьская площадь", "lat": 56.8435, "lon": 60.5935, "description": "Площадь у Драмтеатра."},
            {"name": "Площадь Труда", "type": "SQUARE", "address": "Площадь Труда", "lat": 56.8395, "lon": 60.6085, "description": "Сквер с фонтаном Каменный цветок."},
            {"name": "Улица Бориса Ельцина", "type": "STREET", "address": "ул. Бориса Ельцина", "lat": 56.8455, "lon": 60.5905, "description": "Деловой квартал Екатеринбург-Сити."},
            {"name": "Проспект Ленина", "type": "STREET", "address": "проспект Ленина", "lat": 56.8395, "lon": 60.6155, "description": "Главная артерия города."},
            {"name": "Улица Малышева", "type": "STREET", "address": "ул. Малышева", "lat": 56.8355, "lon": 60.6125, "description": "Одна из старейших и оживленных улиц."}
        ]

        inserted_count = 0
        skipped_count = 0

        for p in places:
            # Check if place already exists
            cur.execute("SELECT id FROM places WHERE name = %s", (p["name"],))
            if cur.fetchone():
                skipped_count += 1
                continue

            # Insert place
            cur.execute(
                """
                INSERT INTO places (name, description, type, address, location, latitude, longitude, created_at, updated_at, is_active, color)
                VALUES (%s, %s, %s, %s, ST_SetSRID(ST_MakePoint(%s, %s), 4326), %s, %s, %s, %s, %s, %s)
                """,
                (
                    p["name"], p["description"], p["type"], p["address"],
                    p["lon"], p["lat"], p["lat"], p["lon"],
                    datetime.now(), datetime.now(), True, "#22C55E"
                )
            )
            inserted_count += 1

        conn.commit()
        cur.close()
        conn.close()
        print(f"Database population completed! Inserted: {inserted_count}, Skipped: {skipped_count}")

    except Exception as e:
        print(f"Error connecting to database: {e}")

if __name__ == "__main__":
    populate_places()
