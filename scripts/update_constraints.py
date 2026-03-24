import psycopg2

DB_HOST = "localhost"
DB_PORT = "5432"
DB_NAME = "uvi"
DB_USER = "postgres"
DB_PASS = "postgres_secret"

def update_constraint():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
        cur = conn.cursor()

        # New list of allowed values for 'type' column
        allowed_types = [
            'RESTAURANT', 'CAFE', 'BAR', 'CLUB', 'CONCERT', 'FESTIVAL',
            'EXHIBITION', 'THEATER', 'CINEMA', 'MUSEUM', 'PARK', 'SPORTS',
            'CONFERENCE', 'WORKSHOP', 'PARTY', 'SIGHT', 'SHOPPING', 'STREET', 'SQUARE', 'OTHER'
        ]

        # Convert list to SQL format
        types_sql = ", ".join([f"'{t}'" for t in allowed_types])

        # Drop the old constraint
        cur.execute("ALTER TABLE places DROP CONSTRAINT places_type_check;")

        # Add the new constraint
        cur.execute(f"ALTER TABLE places ADD CONSTRAINT places_type_check CHECK (type::text = ANY (ARRAY[{types_sql}]::text[]));")

        conn.commit()
        cur.close()
        conn.close()
        print("Constraint updated successfully!")

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    update_constraint()
