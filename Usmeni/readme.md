# Usmeni deo ispita
1. Uvod 
2. Komunikacija u DS
3. Sinhronizacija u DS
4. Konzistencija u DS
5. Vrste replika i protokoli konzistencije
6. Sistemi otporni na greške
7. P2P sistemi
8. Distribuirani fajl sistem
9. HDFS


# Uvod 
> Pitanja sa ispita:
> - Objasni razliku između **mrežnog OS**, **distribuiranog OS** i **OS baziranog na middleware-u**.
> - Kako izgleda poziv **Sun RPC kompajlera**? Koji fajlovi se dobijaju kao rezultat ovog kompajliranja i šta sadrže? Objasniti na primeru.
> - Šta je **transakcija** u distribuiranim informacionim sistemima? Koje osobine treba da zadovolji svaka transakcija da bi bila uspešno izvršena?
> - Koje su prednosti **distribuiranih sistema** u odnosu na **centralizovani**? Zašto je teško ostvariti sinhronizaciju u DS? Zašto je nekada problem detektovati greške u DS?
> - Pre nego što je klijent uputio **RPC poziv** serveru, server mora da bude registrovan. Kako se obavlja registrovanje servera? Šta je **portmapper**? Šta podrazumeva termin **binding** (povezivanje)?
> - Šta se podrazumeva pod **pristupnom**, **lokacionom** i **migracionom** transparentnošću? Objasniti na primeru. 
> - Šta je **IDL** i koja je njegova **uloga** u DS?
> - Šta se podrazumeva pod **skalabilnim** distribuiranim sistemom? 
> - Pobroji redom korake kojima se ostvaruje povezivanje klijenta i servera kod **DCE**.
> - Pobroji razliku između **tranzijentnih** i **perzistentnih** komunikacija u distribuiranom sistemu zasnovanom na middleware-u sa razmenom poruka. 

# Distribuirani sistem - definicija

1. Distribuirani sistem je kolekcija nezavisnih računara koji svojim korisnicima izgledaju kao jedan koheretni (povezan) sistem. 
2. Distribuirani sistem predstavlja HW-ske i SW-ske komponente na umreženim računarima koji komuniciraju i koordinišu svoje aktivnosti isključivo slanjem poruka. 
3. Distribuirani sistem ne može da završi posao ako samo 1 računar u sistemu otkaže. To je najveća mana DS-a, ali postoje načini da se to prevaziđe. 

# Prednosti DS nad Centralizovanim sistemima

- DS imaju bolji odnos cena/performanse od velikih računara. Jedan mainframe računar može da košta mnogo više od izgrađenog sistema od više računara. 
- DS ima veću ukupnu moć obrade nego mainframe računari
- Ako 15% mašina DS-a otkaže, on i dalje radi i izvršava se aplikacija, dok centralizovan sistem ne može da izvršava aplikaciju ako računar otkaže. 
- DS se može postpeneo povećavati dodavanjem novih računara
- DS omogućava da više korisnika pristupa zajedničkim bazama podataka i periferijama, kao i olakšanu komunikaciju između ljudi
- DS omogućava da se opterećenje celog sistema efikasno raspodeli među trenutno dostupnim računarima

# Mane DS u odnosu na Centralizovane sisteme

- DS je mnogo kompleksniji od centralizovanog sistema jer je teže rezviti SW za DS nego za centralizovane sisteme. 
- Ako računari u DS-u nisu korektno povezani ceo sistem može otkazati ili postati zagušen
- Dostupnost i pristup resursima, bazi podataka i periferijama je jednostavna i laka, pa se javljaju problemi bezbednosti 

# Osnovne osobine DS-a

1. Heterogenost
2. Transparentnost
3. Otvorenost
4. Skalabilnost

# Heterogenost DS-a 

Heterogenost DS-a odnosi se na to da je DS sastavljen od heterogenog skupa računara. Harder svakog računara može da se razlikuje, usmislu različitih skupova instrukcija i različite prezentacije podataka. Pored toga, računari mogu da se razlikuju po OS-u i programskim jezicima.

# Transparentnost DS-a

DS treba da skriva od korisnika činjenicu da su njegovi procesi i resursi fizički distribuirani. Transparentnost DS-a omogućava da se DS koristi sa istom lakoćom kao jednoprocesorski sistem. Primer je Web koji nam dozvoljava da pritupimo željenim informacijama, a pri tome ne moramo nužno da znamo gde se ti podaci nalaze fizički.

> Tipovi transparentnosti:
> > 1. **Pristupna transparentnost** - podacima i resursima se treba pristupati na jedinstven način, bez obzira da li su oni smešteni na udaljenom ili lokalnom računaru.
> > 2. **Lokacijska transparentnost** - korisnik ne mora da zna gde se traženi resurs fizički nalazi u sistemu. Ovo se postiže imenovanjem. Primer: URL ime https://www.prenhall.com/index.html ne nagoveštava gde se zapravo nalazi glavni web server izdavačke kuće PrenHall, a korisnici sa bilo koje lokacije na isti način pristupaju tom serveru.
> > 3. **Migraciona transparentnost** - resurs može da promeni svoju lokaciju, a klijent to ne treba da primeti i zna. Resurs može menjati svoju lokaciju, a da pri tom ne dolazi do promene njegovog imena.
> > 4. **Transparentnost konkurencije** - omogućava da više procesa istovremeno koristi isti resurs, a da korisnici ne primete da se isti resurs koristi istovremeno. Istovremeni pristup istom resursu mora ostaviti taj resurs u konzistentnom stanju. To se postiže serijalizacijom deljivog resursa. 
> > 5. **Transparentnost replikacije (umnožavanja)** - omogućava postojanje više kopija istog resursa u DS-u u cilju povećavanja performansi i pouzdanosti. Kopija resursa se postavlja bliže mestu odakle se obavlja pristup tom resursu, a sve kopije imaju isto ime. Korisnici nisu svesni postojanja više kopija resursa. 
> > 6. **Transparentnost na otkaze** - sistem otporan na otkaze mora biti u stanju da izvrši realokaciju resursa na delove sistema koji korektno rade, tako da korisnik ne primeti da je resurs u kvaru. 
> > 7. **Transparentnost paralelizacije** - paralelizacija procesa se izvršava transparentno za aplikativnog programera i korisnika aplikacije. Korisnik ne sme da bude svestan da je i kako je aplikacija paralelizovana i gde se izvršavaju procesi. 

# Otvorenost DS-a 

Otvoreni DS dozvoljava dodavanje novih servisa i omogućava dostupnost servisima od strane različitih klijenata. Npr, Web servisu može se pristupiti preko mnogo različitih klijenata - Chrome, Opera, Explorer itd. 
Otvoreni DS je sistem koji nudi servise tj. usluge. Kod DS-a usluge su definisane interfejsima koji definišu kako se pristupa uslugama. Interfejsi se opisuju pomoću posebnog jezika - **IDL**-a (*Interface Definition Language*).

# Skalabilnost DS-a

Skalabilnost ili proširljivost DS-a posmatra se kroz 3 dimenzije:
- skalabilnost u odnosu na broj korisnika i resursa
- skalabilnost u odnosu na geografsku udaljenost resursa i korisnika
- administrativna skalabilnost (sistem može imati i više administrativnih domena)

Da bi skalabilnost bila izvodljiva, moraju se koristiti **decentralizovani algoritmi**, podaci i usluge. 

Decentralizovani algoritmi imaju sledeće osobine: 
- ni jedna mašina nema kompletnu informaciju o stanju sistema
- mašine donose odluku samo na osnovu lokalnih podataka 
- otkaz jedne mašine ne narušava sistem
- nema pretpostavke o postajanju globalnog časovnika 
