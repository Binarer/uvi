import psycopg2

DB_HOST = "localhost"
DB_PORT = "5432"
DB_NAME = "uvi"
DB_USER = "postgres"
DB_PASS = "postgres_secret"

def get_check_constraint():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
        cur = conn.cursor()

        # Query to find check constraints for the 'places' table
        cur.execute("""
            SELECT conname, pg_get_constraintdef(oid)
            FROM pg_constraint
            WHERE conrelid = 'places'::regclass AND contype = 'c';
        """)

        constraints = cur.fetchall()
        for conname, def_ in constraints:
            print(f"Constraint: {conname}, Definition: {def_}")

        cur.close()
        conn.close()

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    get_check_constraint()
