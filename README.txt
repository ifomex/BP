-----------------------------------------------------------
-        DEFEAT MAP                                       -
-                                                         -
-        Autor:  Petr Blatný                              -
-        E-mail: xblatn03[at]stud.fit.vutbr.cz            -
-        Datum:  16.5.2012                                -
-----------------------------------------------------------

O APLIKACI
----------
Aplikace je lokalizační hra, kde hráčovým úkolem je projít
na mapě zvýrazněnou množinu bodů za co nejkratší čas. Apli-
kace nabízí kompletní správu tratí, tedy jejich vytváření 
editaci i mazání. Hra poskytuje čtyři režimy:
    - Klasický
    - Na čas
    - Podle pořadí
    - Naslepo
Hra je rozdělena na dvě části podle počtu hráčů. Hra pro 
jednoho hráče a pro více hráčů na více zařízeních.    


INSTALACE A SYSTÉMOVÉ POŽADAVKY
-------------------------------
Aplikace pro svůj běh vyžaduje minimální verzi sytému 
Android 2.1. Testována byla do verze systému 2.3.

Instalace se provádí pomocí instalačního balíčku 
DefeatMap.apk. Po otevření tohoto souboru systém Android 
vyvolá instalaci. Aplikace vyžaduje oprávnění:
    ACCESS_COARSE_LOCATION - pro určení přibližné polohy
    ACCESS_FINE_LOCATION - pro upřesnění polohy pomocí GPS
    ACCESS_WIFI_STATE - pro zjištění stavu WIFI
    ACCESS_NETWORK_STATE - pro zjištění stavu sítě
    INTERNET - pro přístup na Internet
    WRITE_EXTERNAL_STORAGE - pro zápis mapových dlaždic na 
          SD kartu

Po schválení oprávnění a dokončení instalace je možné 
aplikaci používat.




NÁVOD K POUŽITÍ
---------------
Aplikace je funkčně rozdělena na tři části. První částí je 
správa tratí, druhou je hra pro jednoho hráče a poslední 
část je hra pro více hráčů. 
  Do každé z těchto částí je možné se dostat z úvodní obra-
zovky pomoci příslušného tlačítka. 
  V celé aplikaci je stejné menu, jehož obsahem jsou položky
Nastavení a Statistiky. V nastavení je možnost nastavit 
zobrazení úvodního dialogu pro povolení GPS, interval 
GPS. Dále je možné zakázat automatické zhasínání obrazovky 
v průběhu hry. Je zde také položka pro zobrazení informací 
o aplikaci. 
  Statistiky zobrazují jsou záložkami rozděleny na Celkové a
Pro trať. V obou případech jsou zobrazeny všechny ukládané 
statistiky. Pro trať jsou zobrazeny po výběru příslušné 
tratě.

Správa tratí
Po přístupu do správy tratí je zobrazeno tlačítko pro vytvo-
ření nové tratě a seznam, jehož položkami jsou jednotlivé 
tratě. Po výběru tratě ze seznamu je zobrazena kontextová 
nabídka s položkami editování a smazání tratě.
  Při vytváření nebo editování tratě je možné zadat název 
tratě. Dále po stisku tlačítka "Přidat bod" je možné z mapy
vybrat jeden bod tratě. Tlačítkem "Generovat bod" se bod 
vygeneruje na aktuálním výřezu mapy. Po stisknutí již vytvo-
řeného bodu je zobrazena kontextová nabídka s editací a 
smazáním bodu. Mapu je možné vycentrovat na aktuální pozici 
pomocí položky z menu.  
  Po vytvoření bodu nebo při jeho editaci, je zadáván název 
bodu, jeho pořadí na trati a doplňující otázka a odpověď.

Hra pro jednoho hráče
Před samotnou hrou je třeba vybrat trať a nastavit typ hry.
Obě informace je možné nastavit pomocí výběru položky z 
rozbalovacích seznamů. Dále je možné povolit nebo zakázat 
otázky u bodů. Po stisku tlačítka "Start" je hra spuštěna.
  Před spuštěním je zobrazen dialog informující o čekání na 
GPS pozici. Po jejím získání se hra automaticky spouští. 
Ve hře jsou na mapě zobrazeny body tratě. Mapa se 
automaticky centruje na aktuální pozici. Dále je zobrazen 
aktuální čas hry a tlačítko na pozastavení a opětovné 
spuštění hry.

Hra pro více hráčů
Ve hře pro více hráčů jsou dvě role. První je vytvářející, 
jenž nastavuje parametry hry a spouští hru. Druhou rolí je 
klient, který se pouze ke hře připojí.
  Stejně jako u hry pro jednoho hráče je i zde nutné 
nastavit typ hry a vybrat trať. Tuto možnost má pouze 
vytvářející. Všichni uživatelé musí vyplnit své hráčské
jméno. Klient navíc ještě vyplňuje číslo hry, ke které se 
připojuje. Tlačítkem "Připojit" se hra spustí.
  Po přechodu do hry je zobrazen dialog s identifikátorem 
hry a seznam připojených uživatelů, který je tvořen jejich  
jménem a příznakem o získané GPS pozici. Pokud mají všichni 
připojení hráči platnou pozici, vytvářející může hru spustit 
tlačítkem "Start". Do již spuštěné hry není umožněno 
připojení dalších hráčů. Ve hře je zobrazena mapa a na ní 
aktuální hráčova pozice, pozice všech oponentů a body tratě.
Po stisknutí ikony protihráče jsou o něm zobrazeny informace 
jako jeho jméno a počet dosažených bodů. Po dokončení hry je 
zobrazen dialog informující o dosaženém čase a dosažené 
vzdálenosti.  
   