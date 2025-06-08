# KoxiakoweGildie

Plugin na Gildie Minecraft.

## Wymagania

- Minecraft 1.16.5 lub nowszy
- Java 8 lub nowsza
- Vault (wymagany)
- PlaceholderAPI (opcjonalny)
- Plugin ekonomiczny (np. EssentialsX) obsługiwany przez Vault

## Instalacja

1. Pobierz najnowszą wersję pluginu z sekcji [Releases](https://github.com/JakisKoxiak/KoxiakoweGildie/releases)
2. Skopiuj plik `.jar` do folderu `plugins` na serwerze
3. Upewnij się, że masz zainstalowane wymagane pluginy (Vault i PlaceholderAPI)
4. Uruchom serwer

## Konfiguracja

### config.yml
```yaml
# Koszt założenia gildii
koszt_zakladania_gildii: 1000.0

# Maksymalna długość nazwy gildii
maksymalna_dlugosc_nazwy: 20

# Maksymalna długość tagu gildii
maksymalna_dlugosc_tagu: 5

# Minimalna długość nazwy gildii
minimalna_dlugosc_nazwy: 3

# Minimalna długość tagu gildii
minimalna_dlugosc_tagu: 2

# Ustawienia placeholderów
placeholdery:
  # Wartość zwracana gdy gracz nie jest w gildii
  brak_gildii:
    nazwa: "Brak"    # Wartość dla %gildie_gracz%
    tag: "Brak"      # Wartość dla %gildie_tag%
    czlonkowie: "0"  # Wartość dla %gildie_czlonkowie%

# Ustawienia bazy danych
baza_danych:
  # Typ bazy danych (sqlite lub mysql)
  typ: "sqlite"
  # Interwał zapisywania w minutach
  interwal_zapisywania: 5
  # Ustawienia MySQL (ignorowane jeśli typ = sqlite)
  mysql:
    host: "localhost"
    port: 3306
    baza: "gildie"
    uzytkownik: "root"
    haslo: "haslo"
```

### messages.yml
Plik zawiera wszystkie wiadomości wyświetlane graczom. Możesz je dostosować do swoich potrzeb.

## Komendy

- `/gildia zaloz <nazwa> <tag>` - Załóż nową gildię
- `/gildia usun <nazwa>` - Usuń gildię
- `/gildia zapros <gracz>` - Zaproś gracza do gildii
- `/gildia wyrzuc <gracz>` - Wyrzuć gracza z gildii
- `/gildia zastepca <gracz>` - Ustaw/usuń zastępcę gildii
- `/gildia info [nazwa]` - Wyświetl informacje o gildii

## Uprawnienia

- `gildie.admin` - Uprawnienia administratora gildii (domyślnie: op)
- `gildie.zarzadzanie` - Uprawnienia do zarządzania gildią (domyślnie: true)

## Integracja z PlaceholderAPI

Jeśli masz zainstalowany PlaceholderAPI, plugin automatycznie zarejestruje następujące placeholdery:

- `%gildie_gracz%` - Nazwa gildii gracza (lub "Brak" jeśli nie jest w gildii)
- `%gildie_tag%` - Tag gildii gracza (lub "Brak" jeśli nie jest w gildii)
- `%gildie_czlonkowie%` - Liczba członków w gildii gracza (lub "0" jeśli nie jest w gildii)

Przykład użycia:
```yaml
# W pliku konfiguracyjnym czatu
format: '&7[%gildie_tag%] &f%player_name%: %message%'
```

## Baza danych

Plugin obsługuje dwie bazy danych:
- SQLite (domyślnie) - dane są przechowywane w pliku `gildie.db` w folderze pluginu
- MySQL - wymaga skonfigurowania połączenia w `config.yml`

## Autor

- JakisKoxiak

## Licencja

Ten projekt jest objęty licencją MIT. Szczegóły znajdziesz w pliku [LICENSE](LICENSE). 
