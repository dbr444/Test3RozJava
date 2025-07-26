Instrukcja obsługi:)
Jak już uruchomisz pierwszy raz apkę, to wejdź proszę do resources i w skryptach liquibase odkomentuj changelog-master i jeszcze raz włącz.
Przygotowałem skrypty, które:
1. Tworzą jednego Admina i jednego Importera na początek żeby można fajnie było dodawać użytkowników
2. Startują odpowiednio id_sequence
3. Tworzą tabele widoków
Nie zdążyłem już po prostu ogarnąć skryptu żeby tworzył te tabele początkowe dla encji i już zostawiłem to hibernateowi:)

Zliczanie logowania - działa na instancję aplikacji. Myślałem żęby zrobić to na bazie encji żeby przy restarcie apki też działało, ale nie kombinowałem już bo takiego wymagania nie było więc póki co tak mam

Import - tu podobnie. Nie byłem pewien czy to w ramach jednej instancji aplikacji ma być lokalnie, więc zrobiłem taką prostszą wersję. Mam nadzieję, że styknie i to i to. Jak nie to będę poprawiać:)

studenci - raz wspomniałeś o ukończonej uczelni, potem o aktualnej. Patrząc na opis, np. rok studiów, stypendium itd. to zakładam, że jednak o aktualną chodziło, więc taka też jest wdrożona, bo ta ukończona chyba przez pomyłkę.

dto - podzieliłem na dwa. Pierwsze to takie zwracane przy tworzeniu, drugie już bezpośrednio mapuję sobie view (już nie robiłem drugiego dto na jego podstawie tylko view temu służy) z dodatkowymi informacjami, których przy tworzeniu niektórych nie ma

co do stanowisk - uznałem, że najlepszym chyba wyjściem jest ich po prostu tworzenie i brak możliwości edycji, żeby były to wpisy historyczne nawet jak się tylko pensja zmienia załóżmy no i tak są zarządzane

w update Employee nie daje edycji position bo do tego mamy ten oddzielny endpoint

co do aktualizacji jeszcze - napisales ze maja być tylko 1 select i 1 update ale jak mam tabele główną i te dziedziczące po niej i zmienię specyficzną cechę z osobnej tabeli dla danej osoby, oprócz tych wspólnych, to chyba nie da się tak zrobić:(

Takich kilka informacji ode mnie. Krytyka (ale i pochwały xd) mile widziana!

#admin i importer:
#username:admin@a.com
#username:importer@i.com
# hasla: 1234