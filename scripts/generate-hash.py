#!/usr/bin/env python3
import bcrypt
import sys

def generar_hash(password):
    password_bytes = password.encode('utf-8')
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password_bytes, salt)
    return hashed.decode('utf-8')

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("❌ Uso: python generar_hash.py <contraseña1> <contraseña2> ...")
        print("   Ejemplo: python generar_hash.py admin user1 user2")
        sys.exit(1)
    
    print("\n🔐 Hashes BCrypt generados:")
    print("=" * 70)
    
    for i, password in enumerate(sys.argv[1:], 1):
        hashed = generar_hash(password)
        print(f"{i}. Contraseña: {password}")
        print(f"   Hash:       {hashed}")
        print("-" * 70)