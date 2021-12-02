# Uvod 

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
- nema pretpostavke o postajanju globalnog časovnika jer u DS-u nije moguće postići tačnu sinhronizaciju časovnika. 

> ## Geografska skalabilnost
> Teško je proširiti DS koji je bio projektovan u okviru LAN, jer je komunikacija u okviru LAN sinhrona (zahtevaoc usluge ili klijent se blokira dok čeka na odgovor servera) i to dobro radi u LAN-u jer je vreme čekanja na server kratko, dok je u WAN mrežama duže i to za 3 reda veličine veće čekanje. 

# Tehnike skaliranja
Postoje 3 tehnike skaliranja tj. proširivanja DS-a:
1. **Skrivanje komunikacionog kašnjenja**
    - Koriste se asinhrone komunikacije umesto sinhronih, pa se klijent ne blokira dok čeka server, nego radi drugi posao dok ne dobije prekid koji obaveštava klijenta da je dobio odgovor servera
    - Asinhrone komunikacije nisu od koristi kod interaktivnih aplikacija. Tada je rešenje download-ovati deo kod na klijentsku stranu da bi se ubrzala obrada. 
2. **Distribucija**
    - Komponente se dele na manje delove, a zatim se ti delovi distribuiraju na više mašina u sistemu. Primer je DNS i Web. 
3. **Replikacija**
    - Pomaže da se poveća dostupnost i balansira opterećenje u sistemu da bi se postigle bolje performanse. U geografski distribuiranim sistemima poželjno je da postoji kopija resursa blizu mesta korišćenja da bi se smanjilo komunikaciono kašnjenje. 

> # Problemi tehnika skaliranja 
> - Postojanje više kopija dovodi do problema konzistencije - modifikacija jedne kopije dovodi do neslaganja sa ostalim kopijama. Da bi sve kopije bile sinhronizovane, potrebna je globalna sinhronizacija koju je gotovo nemoguće postići u distribuiranom sistemu. 

# Middleware 
Najbitnija stvar kod DS sistema je **komunikacija** koja se bazira na slanju poruka i ne postoji zajednički adresni prostor. Da bi se pojednostavio razvoj DS aplikacija potrebno je obezbediti SW koji će programera osloboditi detalja vezanih za interprocesnu komnukaciju, sinhronizaciju, bezbednost itd. 

U OSI referentnom modelu nedostaje jasna razlika između **aplikacije**, **aplikativno - specifičnih protokola** i **protokola opšte namene**. Postoji dosta protokola opšte namene koji su od koristi za mnoge aplikacije, a ne mogu se kvalifikvati kao transportni protokoli. Ti protokoli spadaju u kategoriju **middleware protokola**. Jedna od suluga koju ovi protokoli pružaju je usliuga **autentifikacije** (provere identiteta) i **autorizacije** (prava pristupa). 

Middleware komunikacioni protokoli pružaju viši nivo komunikacionih usluga i oslobađaju aplikativnog programera detalja vezanih za komunikaciju između procesa. Komunikacija je skrivena iza **poziva procedure** (Remote Procedure Call - **RPC**) ili **metoda** (Remote Method Invocation - **RMI**). Može biti i na raspolaganju skup funkcija za razmenu poruka (message oriented).

Platforma na kojoj su izgrađeni DS su **računarske mreže**. **Mrežni OS** pruža mogućnost korisnicima da pristupe uslugama (servisima) koje su locirane na nekoj drugoj mašini. Da bi se olakšalo pisanje distribuiranih aplikacija i njihova integracija u DS, na mrežni OS može se dodati još 1 SW sloj - **Middleware** - *da bi se sakrila heterogenost platforme na kojoj je sistem izgrađen od same aplikacije i da bi se sakrila komunikacija*. 

Middleware sistemi nude kompletan skup usluga (servisa) aplikaciji i ne dozvoljavaju korišćenje ničeg drugog do njihovih interfejsa prema uslugama (servisima). 

# RPC komunikacioni middleware model 
RPC Middleware model zasniva se na pozivu udaljenih procedura - Remote Procedure Call. **Resursi se modeliraju kao procedure (funkcije).**
Ovako se skriva mrežna komunikacija jer procesi mogu da pozivaju procedure čija je implementacija locirana na drugoj mašini. Kada se zove takva procedura, parametri se transparentno prenose do udaljene mašine na kojoj se zatim procedura izvršava, a rezultat izvršavanja se šalje pozivaocu. Proces koji poziva udaljenu proceduru ima utisak da je procedura izvršena lokalno i nije svestan da je došlo do komunikacije kroz mrežu. 

# Objektno orijentisani middleware model 
Resursi se modeluju kao **objekti** koji sadrže **skup podataka** i **funkcija** nad podacima. **Udaljenom resursu se pristupa kao objektu**. Suština je da svaki objekat implementira **interfejs** koji skriva sve unutrašnje detalje objekta od korisnika. 

*Interfejs sadrži samo deklaraciju metoda koje objekat implementira i jedino što proces vidi od objekta je njegov interfejs*. Distribuirani objekti su uglavnom implementirani tako da je svaki objekat lociran na jednoj mašini, a interfejs je raspoloživ na mnogim drugim mašinama. *Kada proces pozove neki metod, interfejs na lokalnoj mašini jednostavno transformiše **poziv** u **poruku** koja se prosleđuje objektu koji zatim izvršava traženi metod i vraća rezultat. Interfejs zatim transformiše odgovor u vrednost koja se vraća i nju može da koristi pozivajući proces. Proces ne mora da bude svestan mrežne komunikacije.* 

# Tipovi DS-a 
- Na arhitekturnom nivou: ***klijent-server*** i ***peer-to-peer***. 
- U odnosu na oblasti primene: 
  - Distribuirani računarski sistemi: ***klasteri*** i ***grid***
  - Distribuirani infromacioni sistemi: ***sistemi za obradu transakcija*** i ***sistemi za integraciju poslovnih aplikacija***
- ***Ugrađeni (sveprisutni) DS***: mobilni računari, ugrađeni (embeded) računari, komunikacioni sistemi

# Distribuirani računarski sistemi 
Koriste se za izvršenje visokoperformansnih zadataka i to su:
1. **Klasteri** - računari se sastoje od grupe sličnih radnih stanica ili PC-eva koji se nalaze na malom rastojanju i povezani su preko veoma brze lokalne mreže. Svaki računar ima isti OS. 
      - Ključna osobina je **HOMOGENOST** jer svi računari imaju isti OS i svi su povezani istom mrežom. 
      - ***Beowulf*** je klaster baziran na Linux OS-u. Sastoji se od grupe računarskih čvorova kojima upravlja jedan master čvor.
      - ***MPI*** - Message Passing Interface - je najčešći tip middleware kod klastera. Middleware sadrži biblioteku funkcija za izvršenje paralelnih programa. 
2. **Grid** - sastoji se od grupe računarskih sistema koji mogu biti locirani na velikom geografskom području, a koji se mogu razlikovati u pogledu HW-a, SW-a i mrežne tehnologije koju koriste.
      - Veoma su **HETEROGENI**, a integracija se postiže preko zajedničke servisno-orijentisane SW arhitekture - middleware-a. Korisnici girda su organizovani u "virutelne organizacije" i oni imaju pravo pristupa resursima koje poseduje virutelna organizacija.
      - middleware grid obezbeđuje pristup resursima iz različitih administrativnih domena i to samo onim osobama i aplikacijama koji pripadaju virtuelnoj organizaciji. Moraju se obezbediti mehanizmi za autentifikaciju korisnika ili aplikacija. 

# Sistemi za obradu transakcija 

***Transakcija je skup operacija koje se obavljaju kao jedna nedeljiva (atomična) operacija.***

***Sistem za obradu transakcija obezbeđuje da sve ili ni jedna operacija u transakciji budu izvršene bez greške.***

Nakon obavljanja transakcije, sistem mora da bude u poznatom **konzistentnom stanju**, tako što obezbeđuje da se ili sve operacije koje su međusobno zavisne izvrše ili da se ni jedna od njih ne izvrši.

> Primer je plaćanje putem platnih kartica - bankomat ili isporuči novac i umanji vrednost na računu - ili se obe operacije obave ili ni jedna. 

> Primer je i sistem za rezervaciju avio karata - potvrđuje se rezervacija, umanjuje broj slobodnih mesta na letu, skida se novac sa odgovarajućeg računa. Ili se sve obalja ili ništa. 

Transakcije se uvek procesiraju na isti način! To rade tako što sistemi za obradu transakcija pružaju isti interfejs za svaku transakciju, bez obzira na korisnika. 

# ACID osobine transakcija 
Svaka transakcija mora da zadovolji ACID test pre nego što se dozvoli modifikacija sadržaja:

1. **A - Atomicy - Atomičnost** - transakcija mora kompletno da se obavi ili da se uopšte ne obvi
2. **C - Consistency - Konzistentnost** - transakcija ne ugrožava skup ograničenja sistema.
3. **I - Isolation - Izolacija** - transakcije koje se istovremeno izvršavaju (konkurentne) moraju da ostave bazu podataka u istom stanju kao da su se izvršile sekvencijalno. Dakle, dve konkurentne transakcije moraju biti serijalizovane (da se izvršavaju u nekom redosledu).
4. **D - Durability - Trajnost** - kada se transakcija jednom obavi, ne može se poništiti i ostaće trajna čak i u slučaju otkazivanja sistema (nestanka struje). Dakle, izvršene transakcije treba čuvati u nepromenljivoj memoriji.

# Ugrađeni (sveprisutni) distribuirani sistemi

Uređaji ove vrste DS-a su uglavnom mali, napajaju se pomoću baterija, mobilni su i imaju uglavnom bežične mreže. Ovde je bitna osobina odsustvo administrativnog upravljanja.
Primer su kućni sistemi, elektronski uređaji za nadzor pacijenta, senzorske mreže (merenje temperature, vlažnosti itd).
