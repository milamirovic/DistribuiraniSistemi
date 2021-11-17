# MPI - Message Passing System

MPI je standardizovan prenosiv **sistem za razmenu poruka**. Namenjen je za podršku komunikacijama u paralelnim sistemima da bi se poboljšale performanse izvršenja aplikacije. MPI je nastao iz potrebe za definisanjem standarda nezavisnog od hardverske platforme. 

MPI nije programski jezik, on definiše sintaksu i semantiku bibliotečkih funkcija korisnih za pisanje prenosivih programa za razmenu poruka. 

Glavni cilj MPI-a je postizanje prenosivosti sistema na različite mašine. 

**MPI program** podrazumeva skup procesa, gde svaki proces obavlja neki zadatak nad svojim lokalnim podacima. Zadatak koji obavlja svaki proces je definisan je kodom programa. 

Razmena podataka između procesa obavlja se korišćenjem funkcija koje se nalaze u MPI biblioteci. 

Procesi mogu da pripadaju **grupama** i grupe omogućavaju da se MPI operacije mogu ograničiti isključivo na procese koji pripadaju jednoj grupi. 
* Svakoj **grupi** se dodeljuje **ID**. 
* Svakom **procesu** unutar grupe se dodeljuje **ID**. 

**Tako da, par [ID_grupe, ID_procesa] jedinstveno identifikuje izvor/odredište poruke.**
U sistemu može biti više grupa procesa koje obavljaju neko izračunavanje i koje se izvršavaju u isto vreme. 
# MPI program

Sastoji se od više instanci sekvencijalnog programa koji komuniciraju pomoću funkcija MPI biblioteke. ove funkcije se mogu svrstati u 4 grupe:
1. funkcije koje vrše **inicijalizaciju, upravljanje i okončavanje komunikacija**
2. funkcije koje služe  za **komunikaciju između para procesa** 
3. funkcije koje izvode **operacije nad grupom procesa**
4. funkcije za **kreiranje proizvoljnih (izvedenih) tipova podataka**
5. funkcije za kreiranje novih **komunikatora** (grupa procesa koji komuniciraju)
## Opšta struktura

Svaki MPI program ima sledeću opštu strukturu:
* uključivanje MPI header file-a
* deklaracija promenljivih
* inicijalizacija MPI okruženja 
* odgovarajuća izračunavanja i pozivi MPI funkcija
* zatvaranje svih MPI komunikacija
>## MPI header
>Sadrži prototipove MPI funkcija kao i definicije makroa, specijalnih konstanti i tipovi podataka korišćenih od strane MPI. Odgovarajuća #include direktiva mora postojati u bilo kom programu koji koristi MPI funkcije ili kontstante. 
>```javascript
>#include <mpi.h>
>```
>Inicijalizacija MPI okruženja obavlja se pozivom funkcije:
>```javascript
>MPI_Init(&argc, &argv)
>```

>## Zatvaranje svih MPI komunikacija 
>Zatvaranje svih MPI komunikacija obavlja se pozivom funkcije:
>```javascript
>MPI_Finalize()
>```
>Posle poziva ove funkcije, ni jedan poziv bilo koje druge MPI funkcije ne može biti izveden. Ako svi procesi ne izvrše ovu funkciju, program se blokira. 
## MPI konvencije 

Imena svih MPI funkcija, konstanti, tipova počinju sa **MPI_**:
###  Imena funkcija imaju ovakav izgled:
```
MPI_ImeFunkcije(argumenti)
```
>Svaka MPI funkcija vraća INT vrednost koja ukazuje na moguću grešku. Ako je povratna vrednost jednaka MPI_SUCCESS, onda nije došlo do greške, u suprotnom jeste. 
### Imena konstanti i osnovnih MPI tipova podataka sastoje se od velikih slova 
```
MPI_COMM_WORLD, MPI_INT, MPI_FLOAT, MPI_CHAR, ...
```
### Imena specijalnih MPI tipova
```
MPI_Comm, MPI_Datatype
```

## Osnovni MPI tipovi podataka 

| MPI_Datatype |C type  |
|--|--|
| MPI_CHAR | signed char |
| MPI_SHORT | signed short int|
| MPI_INT | signed int|
| MPI_LONG | signed long int|
| MPI_UNSIGNED_CHAR | unsigned char|
| MPI_UNSIGNED_SHORT | unsigned short int|
| MPI_FLOAT | float|
| MPI_DOUBLE | double|
| MPI_LONG_DOUBLE | long double|
| MPI_BYTE | none|
| MPI_PACKED | none|
>Pravilo je da MPI tip podataka koji se nalazi u pozivu funkcije ***MPI_Recv** (funkcija za primanje podataka)* mora da se poklopi sa MPI tipom podataka u pozivu ***MPI_Send** (funkcija za slanje podataka*.
MPI omogućava da se definiše ***izveden tip*** podataka koji se izvodi iz osnovnog. 


# MPI Komunikatori

U MPI-u postoji jedinstvena identifikacija procesa prilikom komunikacije sa drugim procesima. Dovoljno je znati samo identifikator procesa da bi se sa njim obavila komunikacija. 

***Komunikatori su grupe procesa koji mogu međusobno komunicirati.*** 

Procesi mogu komunicirati samo ako dele komunikator. 

U okviru svakog komunikatora ***identifikatori procesa*** uzumaju uzastopne vrednosti (od 0 do brojProcesa-1). Ta vrednost zove se ***rang (rank)*** procesa. 

Osnovni ili globalni MPI komunikator je **MPI_COMM_WORLD**. Po definiciji, svi procesi su deo komunikatora MPI_COMM_WORLD. Korišćenjem ovog komunikatora svaki proces može da komunicira sa svim ostalim procesima u okviru njega. 

U programu može biti definisano više komunikatora i jedan proces može biti član više njih. U okviru svakog komunikatora, proces za identifikatore tj. rank uzimaju vrednosti od 0 do ukupanBrojProcesa-1.

![enter image description here](https://i.imgur.com/2jJgc2N.jpg)
 
### MPI_Comm_rank funkcija
```
int MPI_Comm_rank(MPI_Comm comm, int *rank)
```
> Proces određuje svoj rank u komunikatoru korišćenjem MPI_Comm_rank funkcije
> Isti proces može imati različite za rank u različitim komunikatorima. 

### MPI_Comm_size funkcija
```
int MPI_Comm_size(MPI_Comm comm, int *size)
```
> Proces može odrediti veličinu komunikatora kome pripada pomoću funkcije MPI_comm_size


## Point-to-point komunikacija

Komunikacija tipa: jedan proces šalje poruku, a drugi prima tu poruku. Point-to-point komunikacija između dva procesa uključuje komponente:

- pošiljaoca
- primaoca
- podaci same poruke
- oznaka poruke (message tag)
- komunikator
```
int MPI_Send(void *buf, int count, MPI_Datatype dtype, int dest, int tag, MPI_Comm comm);
```
> ***buf*** je mesto u memoriji odakle počinje slanje ***count*** podataka tipa ***dtype***. Broj podataka u MPI_Recv pozivu treba da bude veći ili jednak broju count u MPI_Send.

> ***dest*** je rank procesa kome se šalje poruka

> ***tag*** je proizvoljan broj koji služi za prepoznavanje odgovarajuće poruke na prijemu (mora da bude isti  u MPI_Send i MPI_Recv koji razmenjuju istu poruku)

> ***comm*** je komunikator u okviru koga se odvija komunikacija

```
int MPI_Recv(void *buf, int count, MPI_Datatype dtype, int source, int tag, MPI_Comm comm, MPI_Status *status);
```
> ***buf*** je mesto u memoriji od kojeg počinje poruka koja je primljena. 

> Maksimalni broj podataka tipa ***dtype*** koji se prima određen je sa ***count***.

> ***source*** je rank izvora poruke

> ***tag*** je oznaka poruke 

> ***comm*** je komunikator u kojem moraju biti oba procesa

> ***stat*** kog je tipa MPI_Status, to je obavezni argument funkcije

Primer:
***MPI_Recv(c, 10, MPI_INT, 1, 10, MPI_COMM_WORLD, &stat);***
> Prima 10 elementa tipa MPI_INT u niz c, prima to od procesa 1. Oznaka poruke je 10, komunikator u okviru koga se poruka salje je globalni komunikator. 

***MPI_Recv(&d, 1, MPI_DOUBLE, 0, 19, Comm1, &stat);*** 
> Vrsi prijem u jednu promenljivu d, prima se 1 podatak MPI_DOUBLE od procesa 0. Tag poruke je 19. Komunikator je Comm1. 

Moguće je da se koristi opcija da MPI?Recv prima sa bilo kog procesa i bilo koji tag poruke. To se postiže pomoću MPI_ANY_SOURCE i MPI_ANY_TAG. U tom slučaju argument **status** daje informaciju o izvoru i oznaci poruke, kao i broju podataka koji su primljeni. TO se dobija sa **status.MPI_TAG** i **status.MPI_SOURCE**.

***Maximalno se može primiti count podataka! Pošiljalac i primalac moraju da se slažu po tipu podataka!***

## Kako radi MPI_Send?

Postoje 2 scenarija, zavisno od veličine poruke koja se šalje:

1. Ako je veličina poruke tolika da može da stane u sistemski bafer na strani ***primaoca***, poruka se kopira u taj bafer. Proces koji je pozvao MPI_Send se vraća i nastavlja sa daljim izvršavanjem, za to vreme može da radi nešto drugo. Kada se u procesu primaoca javi odgovarajući poziv MPI_Recv poruka se kopira u user bafer tj. u promenljivu buf. 
2. Ako je veličina poruke veća od veličine sistemskog bafera komunikacija se obavlja bez baferovanja - MPI_Send čeka da se poruka isporuči direktno u user bafer primaoca (MPI_Send se blokira dok proces primaoc ne dođe do MPI_Recv u svom izvršenju). 

## Kako radi MPI_Recv?

Proces koji pozove MPI_Recv se vraca daljem izvrsenju tek kada poruka koja se očekuje da bude primljena bude u user baferu tj. u promenljivoj buf. MPI_Recv se blokira dok god poruka ne bude u buf promenljivoj. 

Neka proces P0 ima MPI_Recv(od procesa P1) i MPI_Send(ka procesu P1).
Neka proces P1 ima MPI_Recv(od P0) i MPI_Send(ka procesu P0). 

Šta će da se desi? Pa upareni su MPI_Recv i MPI_Send procesa P0 i P1, kao i MPI_Send i MPI_Recv procesa P1. Javlja se uzajamno blokiranje.
