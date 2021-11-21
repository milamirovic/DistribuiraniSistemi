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
>```c
>#include <mpi.h>
>```
>Inicijalizacija MPI okruženja obavlja se pozivom funkcije:
>```c
>MPI_Init(&argc, &argv)
>```

>## Zatvaranje svih MPI komunikacija 
>Zatvaranje svih MPI komunikacija obavlja se pozivom funkcije:
>```c
>MPI_Finalize()
>```
>Posle poziva ove funkcije, ni jedan poziv bilo koje druge MPI funkcije ne može biti izveden. Ako svi procesi ne izvrše ovu funkciju, program se blokira. 
## MPI konvencije 

Imena svih MPI funkcija, konstanti, tipova počinju sa **MPI_**:
###  Imena funkcija imaju ovakav izgled:
```c
MPI_ImeFunkcije(argumenti)
```
>Svaka MPI funkcija vraća INT vrednost koja ukazuje na moguću grešku. Ako je povratna vrednost jednaka MPI_SUCCESS, onda nije došlo do greške, u suprotnom jeste. 
### Imena konstanti i osnovnih MPI tipova podataka sastoje se od velikih slova 
```c
MPI_COMM_WORLD, MPI_INT, MPI_FLOAT, MPI_CHAR, ...
```
### Imena specijalnih MPI tipova
```c
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
```c
int MPI_Comm_rank(MPI_Comm comm, int *rank)
```
> Proces određuje svoj rank u komunikatoru korišćenjem MPI_Comm_rank funkcije
> Isti proces može imati različite vrednosti za rank u različitim komunikatorima. 

### MPI_Comm_size funkcija
```c
int MPI_Comm_size(MPI_Comm comm, int *size)
```
> Proces može odrediti veličinu komunikatora kome pripada pomoću funkcije MPI_Comm_size


# Point-to-point komunikacija

Komunikacija tipa: jedan proces šalje poruku, a drugi prima tu poruku. Point-to-point komunikacija između dva procesa uključuje komponente:

- pošiljaoca
- primaoca
- podaci same poruke
- oznaka poruke (message tag)
- komunikator
```c
int MPI_Send(void *buf, int count, MPI_Datatype dtype, int dest, int tag, MPI_Comm comm);
```
> ***buf*** je mesto u memoriji odakle počinje slanje ***count*** podataka tipa ***dtype***. Broj podataka u MPI_Recv pozivu treba da bude veći ili jednak broju count u MPI_Send.

> ***dest*** je rank procesa kome se šalje poruka

> ***tag*** je proizvoljan broj koji služi za prepoznavanje odgovarajuće poruke na prijemu (mora da bude isti  u MPI_Send i MPI_Recv koji razmenjuju istu poruku)

> ***comm*** je komunikator u okviru koga se odvija komunikacija

```c
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

# Point-to-point komunikacija BEZ deadlock-a (uzajamnog isključivanja)
```c
#include <stdio.h>
#include <mpi.h>

void main (int argc, char **argv) 
{
    int myrank; //id procesa
    MPI_Status status;
    int x, y;
    MPI_Init(&argc, &argv); //inicijalizacija MPI
    MPI_Comm_rank(MPI_COMM_WORLD, &myrank); //proces pribavlja svoj rank
    if(myrank == 0) 
    {
        //ako je u pitanju proces P0
        x = 3;
        MPI_Recv(&y, 1, MPI_INT, 1, 19, MPI_COMM_WORLD, &status);
        MPI_Send(&x, 1, MPI_INT, 1, 17, MPI_COMM_WORLD);
    }
    else if(myrank == 1)
    {
        x = 5;
        MPI_Send(&x, 1, MPI_INT, 0, 19, MPI_COMM_WORLD);
        MPI_Recv(&y, 1, MPI_INT, 0, 17, MPI_COMM_WORLD, &status);
    }
    
    printf("Proces %d y=%d", myrank, y);
    MPI_Finalize();
}
```

*Sta se dešava ovde?*
Proces P0 dodeli vrednost x = 3 i iza toga čeka na odgovor procesa P1 sa MPI_Recv (blokira se na MPI_Recv ako u P1 jos nije došlo do MPI_Send). Istovremeno se izvršava proces P1 koji svoj x postavlja na 5. P1 stiže do MPI_Send, šalje svoje x = 5, od adrese x, jedan MPI_INT element. Šalje ga procesu 0, a oznaka poruke je 19. Sve navedeno odgovara MPI_Recv pozivu u P0, pa se to što šalje P1 prima u promenljivu y procesa P0. Zatim se u P0 izvršava MPI_Send, kojim se šalje vrednost za x iz procesa P0, a to je 3, šalje procesu P1 sa tagom 17. Pa se poruka prima u P1 u promenljivu y procesa P1. 

## Zadatak 1
*Napisati program koji uzima podatke od nultog procesa i šalje ih svim drugim procesima tako što ***proces i*** treba da primi podatke i pošalje ih ***procesu i+1***, sve dok se ne stigne do poslednjeg procesa. Unos podataka se završava nakon što se prenese negativna vrednost podatka od prvog do poslednjeg procesa.*

U procesu P0 stalno se učitavaju vrednosti u promenljivu **value** i ta se vrednost šalje procesu P1. To se radi pomoću scanf i MPI_Send. 
U procesu P1, primi se poruka od prethodnog procesa sa MPI_Recv i prosledi taj podatak sledećem. I tako se radi sve do poslednjeg priocesa koji samo prima poruku od prethodnog procesa. 
Vrednosti se učitavaju u P0 sve dok je vrednost veća ili jednaka od 0. 

```c
#include "mpi.h"
#include <stdio.h>

int main(int argc, char **argv) 
{
    int rank, value, size; 
    //rank je id procesa, value je promenljiva u koju primamo i saljemo podatke
    //size sluzi da razdvojimo one koji nisu poslednji proces
    MPI_Status status;
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);//svaki proces dobija svoj rank
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    do
    {
        if(rank == 0)
        {
            scanf("%d", &value); //učitamo vrednost
            MPI_Send(&value, 1, MPI_INT, rank + 1, 0, MPI_COMM_WORLD); 
            //šaljemo je procesu P1
        }
        else
        {
            //ako nije proces P0
            MPI_Recv(&value, 1, MPI_INT, rank -1, 0, MPI_COMM_WORLD, &status);
            //primimo vrednost
            if(rank < size - 1)
            {
                //ako je rank manji od poslednjeg ranka
                //šaljemo vrednost sledećem rankuđ
                //poslednji rank nema kome da šalje
                MPI_Send(&vlue, 1, MPI_INT, rank + 1, 0, MPI_COMM_WORLD);
            }
        }
        printf("Proces P%d ima %d\n", rank, value);
    }while(value >= 0);
    MPI_Finalize();
    return 0;
}
```

## Zadatak 2
*Napisati program koji nalazi sumu prvih n celih brojeva korišćenjem point-to-point komunikacije, tako da svaki proces učestvuje u sumiranju.*

P0 ima value(0) = rank(0) + 1 = 1
P1 ima value(1) = value(0) + (rank(1) + 1) = 3
P2 ima value(2) = value(1) + (rank(2) + 1) = 4
...

P0 šalje value sledećem procesu P1
P1 prima value od P0 i šalje novi value procesu P2
Svi procesi na dalje rade isto što i P1, primaju od prethodnog i novu vrednost šaju sledećem
Poslednji samo prima vrednost i kreira novu vrednost, koju nigde ne šalje. 

```c
#include <stdio.h>
#include "mpi.h"

int main(int argc, char **argv) 
{
    int myrank, numprocs, value;//numprocs je broj procesa
    int sum = 0;
    MPI_Status status;
    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
    MPI_Comm_rank(MPI_COMM_WORLD, &myrank);
    if(myrank == 0) 
    {
        value = 1;
        MPI_Send(&value, 1, MPI_INT, myrank + 1, 0, MPI_COMM_WORLD);
    }
    else 
    {
        if(myrank < numprocs - 1)
        {
            //ako nije poslednji proces 
            MPI_Recv(&value, 1, MPI_INT, myrank - 1, 0, MPI_COMM_WORLD, &status);
            sum = myrank + 1 + value;
            MPI_Send(&sum, 1, MPI_INT, myrank + 1, 0, MPI_COMM_WORLD);
        }
        else
        {
            MPI_Recv(&value, 1, MPI_INT, myrank - 1, 0, MPI_COMM_WORLD, &status);
            sum = myrank + 1 + value;
            printf("my rank is %d, and final sum is %d\n", myrank, sum);
        }
    }
    MPI_Finalize();
}
```
> Ovo nije najoptimalniji način računanja sume. Zato se koriste drugi, bolji načini za sumiranje n celih brojeva. 

MPI podržava komunikaciju bez blokiranja, u kome jedan proces može započeti operaciju slanja ili prijema poruke, a nakon toga se može nastaviti sa obavljanjem drugog posla, pa se posle toga vraća da proveri završetak tj. status operacije. Ovde se slanje i prijem obavlja u 3 koraka:
1. Iniciranje send/recv operacije pozivom funkcije ***MPI_Isend()/MPI_Irecv()*** (I je od Immidiately). 
2. Obavljanje nekog posla tokom vremena komuniciranja 
3. Čekanje na kompletiranje ili testiranje kompletiranja komunikacija korišćenjem funkcija ***MPI_Wait()*** ili ***MPI_Test()***.

## MPI_Isend() funkcija
Iz funkcije MPI_Isend() proces se vraća odmah, pre nego što poruka bude iskopirana u bafer. Sintaksa funkcije za slanje bez blokiranja je:
```c
int MPI_Isend(void *buf, int count, MPI_Datatype dtype, int dest, int tag, MPI_Comm comm, MPI_Request *request)
```

> **buf** je adresa odakle kreće slanje 

> **count** je koliko podataka se šalje

> **dtype** je tip podataka koji se šalju 

> **dest** je destinacija

> **tag** je oznaka poruke koja se šalje

> **comm** je komunikator

> promenljiva **request** je identifikator komunikacionog događaja. Na osnovu request se proverava (testira) status inicirane operacije ili kompletira njeno izvršenje. 

> Program ne sme da modifikuje promenljivu buf nakon iniciranja operacije, sve dok MPI_Wait ili MPI_Test funkcija ne daju pozitivnu informaciju o kompletiranju operacije identifikovane na request. 

## MPI_Irecv() funkcija

Proces vrši prijem bez blokiranja, tj. inicira prijem pomoću funkcije:

```c
int MPI_Irecv(void *buf, int count, MPI_Datatype dtype, int source, int tag, MPI_Comm comm, MPI_Request *request);
```

***Iz ove funkcije se proces vraća odmah, bez potrebe za čekanjem da poruka bude smeštena u prijemni bafer. Za razliku od MPI_Recv ova funkcija ne vraća informaciju o statusu. Informacije o statusu se dobijaju pozivom funkcija MPI_Wait i MPI_Test. ***

> **buf** je adresa odakle kreće prijem

> **count** je broj elemenata koji će se primiti

> **dtype** je tip podataka koji se primaju

> **source** je id procesa od koga dolaz podaci

> **tag** je oznaka poruke

> **comm** je komunikator

> **request** je identifikator komunikacionog događaja

## MPI_Wait() funkcija 

Funkcija koja se koristi za proveru kompletiranja operacija bez blokiranja je:
```c
int MPI_Wait(MPI_Request * request, MPI_Status *status)
```
> Proces se iz ove funkcije vraća onda kada se operacija identifikovana sa **request** izvrši. Ako je inicirana operacija *MPI_Irecv()*, onda ***MPI_Status status*** čuva informaciju o izvoru poruke, oznaci poruke - tag, kao i broju primljenih podataka. U slučaju *MPI_Isend()*, status čuva informaciju o grešci. Ova operacija je blokirajuća. 

## MPI_Test(MPI_Request *request, int *flag, MPI_Status *status)

Funkcija MPI_Test vraća informaciju o trenutnom stanju operacije koja je identifikovana argumentom request:

```c
int MPI_Test(MPI_Request *request, int *flag, MPI_Status *status);
```

Argument **flag** je ***true*** ako je operacija završenja, u suprotnom je ***false***. Argument **status** sadrži dodatne statusne informacije. Ova operacija nije blokirajuća. 

### Primer

```c
#include <stdio.h>
#include <mpi.h>

void main(int argc, char **argv) 
{
    int myrank, x, y;
    MPI_Request request;
    MPI_Status status;
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &myrank);
    if(myrank == 0)
    {
        x = 3;
        MPI_Irecv(&y, 1, MPI_INT, 1, 19, MPI_COMM_WORLD, &request);
        MPI_Send(&x, 1, MPI_INT, 1, 17, MPI_COMM_WORLD);
        MPI_Wait(&request, &status);
    }
    else if(myrank == 1)
    {
        x = 5;
        MPI_Irecv(&y, 1, MPI_INT, 0, 17, MPI_COMM_WORLD, &request);
        MPI_Send(&x, MPI_INT, 0, 19, MPI_COMM_WORLD);
        MPI_Wait(&req, &status);
    }
    printf("Proces %d y=%d", myrank, y);
    MPI_Finalize();
}
```

# Grupne (collective) operacije

***Grupne operacije su operacije koje se primenjuju nad svim članovima jedne grupe.***

***Operacija se izvršava kada svi procesi pozovu odgovarajuću operaciju sa svojim parametrima. Svaki proces mora da pozove grupnu operaciju da bi se ona obavila.***

Grupne operacije dele se na:
* **operacije za kontrolu procesa**, 
* **operacije za globalna izračunavanja**
* **operacija za prenos podataka**

## Grupne operacije za kontrolu procesa 
> ### MPI_Barrier funkcija
> ```c
> int MPI_Barrier (MPI_Comm comm);
> ```
>> **Funkcija MPI_Barrier implementira sinhronizacioni mehanizam poznat kao *barijera*. Proces se blokira na toj naredbi dok svi ostali procesi iz grupe ne dođu do te naredbe. Tada se svi procesi vraćaju daljem izvršenju.**
>> ![enter image description here](https://i.imgur.com/dV8x7U3.jpg)

 ## Operacije za globalna izračunavanja
 Ovde spadaju *operacije za redukciju podataka* i *operacija scan*. Operacija redukcije uzima podatke u ulaznim baferima svih procesa, primenjuje nad njima datu operaciju redukcije i smešta rezultat u root procesa. 
> ### MPI_Reduce funkcija
> ```c
> int MPI_Reduce(void * send_buffer, void * recv_buffer, int count, MPI_Datatype dtype, MPI_Op operation, int rank, MPI_Comm comm);
> ```
>> **send_buffer** je adresa bafera svih procesa gde se nalaze podaci nad kojima se obavlja operacija redukcije. Svaki proces će da ima u grupi svoj send_buffer. Reduce operacija će uzeti podatke u ulaznim baferima svih procesa i nad njima vrši redukciju pomoću operacije operation. 
>> **recv_buffer** je adresa bafera root procesa. U njega Reduce funkcija smešta rezultat. To je bafer samo jednog procesa (čiji je rank naveden kao argument funkcije), ne svih procesa! 
>> **count** je broj podataka u send i receive baferu nad kojima se obavlja operacija redukcije
>> **dtype** je tip podataka u send i receive baferu
>> **comm** je komunikator
>> **operation** - operacija redukcije je definisana tim argumentom
>> **rank** je identifikator root procesa

>> **Operation** može da ima sledeće vrednosti:
>> | Vrednost | Značenje |
>> |--|--|
>> | MPI_MAX | računanje maksimalne vrednosti od svih vrednosti u send_buff |
>> | MPI_MIN | računanje minimalne vrednosti od svih iz send_buff |
>> | MPI_SUM | suma svih vrednosti iz send_buff |
>> | MPI_PROD | proizvod |
>> | MPI_LAND | logički and |
>> | MPI_BAND | bit and |
>> | MPI_LOR | logički or |
>> | MPI_BOR | bit or |
>> | MPI_LXOR | logički xor |
>> | MPI_BXOR | bit xor |
>> | MPI_MINLOC | pored računanja min, daje i rank procesa koji ima tu vrednost (send/recv buff su strukture podataka!) |
>> | MPI_MAXLOC | pored računanja max vrednosti, daje i rank procesa koji ima tu vrednost |

>>> ***Primer Reduce funkcije kada je source jedna vrednost (count=1)***
>>> ```c
>>> #include <stdio.h>
>>> #include <mpi.h>
>>> void main(int argc, char ** argv)
>>> {
>>>     int rank;
>>>     int source, result, root;
>>>     MPI_Init(&argc, &argv); //inicijalizacija programa
>>>     MPI_Comm_rank(MPI_COMM_WORLD, &rank); //svaki proces dobija svoj rank
>>>     root = 7; //root moze da bude bilo koji broj od aktivnih procesa
>>>     source = rank + 1; //source ovako postaje za broj veci od trenutnog id-a procesa
>>>     
>>>     MPI_Reduce(&source, &result, 1, MPI_INT, MPI_PROD, root, MPI_COMM_WORLD);
>>>     
>>>     //svi izvrsavaju MPI_Reduce operaciju, iz svakog procesa se uzme 1 vrednost iz source 
>>>     //(a to su vrednosti rank+1 za svaki proces: 1, 2, 3, 4, ...)
>>>     //pa se izvrsava nad svim tim vrednostima MNOZENJE i generise se result u procesu koji je oznacen sa root
>>>     
>>>     if(rank == root) 
>>>     {
>>>          //ovo pitamo jer samo u root-u se nalazi result, drugi procesi nemaju result
>>>          //da drugi procesi stampaju result bilo bi nan ili null
>>>          printf("PE: %d MPI_PROD result is %d\n", rank, result);
>>>     }
>>>     MPI_Finalize();
>>> }
>>> ```
>>> 
>>> ***Primer Reduce funkcije kada je source niz više vrednosti (count>1)***
>>>  ![enter image description here](https://i.imgur.com/napJfaA.jpg)
>>>  Dat je niz od 2 elementa, svaki proces ima svoj niz. Nulti proces ima niz [5, 1], prvi [2, 3], ... Rezultat (ako se koristi MPI_SUM operacija) bi bio ponovo niz od 2 elementa [18, 14]. Broj elemenata niza nije bitan, može biti proizvoljan, bitan je način računanja sume po elementima niza tj. sama reduce funkcija. 
>>> ```c
>>> #include <stdio.h>
>>> #include <mpi.h>
>>> void main(int argc, char ** argv)
>>> {
>>>     int rank;
>>>     int source, result, root, n, a, b;
>>>     n = 2;//broj elemenata niza
>>>     MPI_Init(&argc, &argv); //inicijalizacija programa
>>>     MPI_Comm_rank(MPI_COMM_WORLD, &rank); //svaki proces dobija svoj rank
>>>     root = 7; //root moze da bude bilo koji broj od aktivnih procesa
>>>     source = rank + 1; //source ovako postaje za broj veci od trenutnog id-a procesa
>>>     
>>>     MPI_Reduce(a, b, n, MPI_INT, MPI_SUM, root, MPI_COMM_WORLD);
>>>     
>>>     if(rank == root) 
>>>     {
>>>          printf("PE: %d MPI_PROD result is %d\n", rank, result);
>>>     }
>>>     MPI_Finalize();
>>> }
>>> ```

> ### MPI_Scan funkcija
> **Ova funkcija se još zove i *prefix reduce operacija*. Funkcija vraća u receive bafer procesa sa rankom *i* redukciju vrednosti u send baferima sa rangovima *0, 1, ... i*.**
> ```c
> int MPI_Scan(void* send_buffer, void* recv_buffer, int count, MPI_Dataype dtype, MPI_Op operation, MPI_Comm comm);
> ```
> Razlike u odnosu na MPI_Reduce su: 
> * Nema rank-a procesa u cijem ce recv_buffer-u da se pamti rezultat, jer se zna da rank ima vrednost i
> ```c
> #include <stdio.h>
> #include <mpi.h>
> void main(int argc, char** argv[])
> {
>     int rank;
>     int source, result;
>     MPI_Init(&argc, &argv);
>     MPI_Comm_rank(MPI_COMM_WORLD, &rank);
>     source = rank + 1;
>     
>     MPI_Scan(&source, &result, 1, MPI_INT, MPI_SUM, MPI_COMM_WORLD);
>     
>     printf("PE %d SUM %d \n", rank, result);
>     MPI_Finalize();
> }
> ```
> Ako imamo 4 procesa, source za svaki proces redom ce biti 1, 2, 3 i 4. Rezultat u tom slučaju je: PE 3 SUM 10, PE 1 SUM 3, PE 0 SUM 1, PE 2 SUM 6 (ne mora da se izvršava redom od procesa 0 do 3, već može i ovako)

## Grupne operacije za prenos podataka
 
Osnovne operacije za prenos podataka su:
* **MPI_Bcast** ili broadcast funkcija, 
* **MPI_Scatter** i 
* **MPI_Gather**.

> ### **MPI_Bcast funkcija**
> Omogućava kopiranje podataka iz memorije root procesa u mesta u memoriji ostalih procesa. Root proces je argument MPI_Bcast funkcije. FUnkciju MPI_Bcast moraju da pozovu svi procesi koji pripadaju datom komunikatoru comm. 
> ```c
> int MPI_Bcast(void* buffer, int count, MPI_Datatype dtype, int rank, MPI_Comm comm);
> ```
> Od adrese buffer procesa sa rankom rank, šalje se count podataka tipa dtype svim procesima u okviru komunikatora comm. To je dejstvo ove funkcije. 
> ![enter image description here](https://i.imgur.com/pG3qeVQ.jpg)
> Neka je A skup podataka koji se šalje iz root procesa (root proces ima rank 0). Kada se uradi MPI_Bcast ono šro se događa je da svaki ostali proces u tom komunikatoru dobija isti taj skup podataka A. 
>> Primer.
>> ```c
>> #include <stdio.h>
>> #include <mpi.h>
>> void main(int argc, char** argv)
>> {
>>       int rank;
>>       int root = 5;
>>       double param; //promenljiva koja će dobiti vrednost u svakom procesu pomoću bcast-a
>>       MPI_Init(&argc, &argv);
>>       MPI_Comm_rank(MPI_COMM_WORLD, &rank);
>>       if(rank == root)
>>       {
>>          param = 23.0;
>>       }
>>       MPI_Bcast(&param, 1, MPI_DOUBLE, 5, MPI_COMM_WORLD);
>>       //od adrese param-a šalje se 1 podatak tipa MPI_DOUBLE (jer je param dobule) iz procesa 5, svim procesima u komunikatoru MPI_COMM_WORLD
>>       printf("Process: %d after bradcast parameter is %f\n", rank, param);
>>       MPI_Finalize();
>> }
>> ```
>> Rezultat će biti da svaki proces štampa 23 jer će svi to dobiti preko bcast-a. Tačnije, svaki proces će za svoju promenljivu param imati vrednost 23. 
> ### **MPI_Scatter funkcija**
> Omogućava da i-ti segment bafera root procesa bude poslat i-tom procesu u grupi gde su svi segmenti iste veličine. 
> ```c
> int MPI_Scatter(void* send_buffer, int send_count, MPI_Datatype send_type, void* recv_buffer, int recv_count, MPI_Datatype recv_type, int rank, MPI_Comm comm)
> ```
> Praktično se u okviru send_buffer-a procesa čiji je rank dat nalaze podaci i prilikom izvršenja ove funkcije se svakom procesu (i proces sa datim rankom rank) šalje po send_count podataka tipa send_type. Svaki proces dobija isti broj podataka tog tipa u svoj recv_buffer. 
> * **send_buffer** je adresa bafera root procesa odakle počinje slanje podataka
> * **send_count** je broj podataka koji se šalje svakom procesu (broj podataka u segmentu)
> * **send_type** je tip podataka koji se šalju
> * **recv_buffer** je adresa prijemnog bafera
> * **recv_count** je broj podataka koji se primaju u recv_buffer
> * **recv_type** tip podataka u prijemnom baferu
> * **rank** je id root procesa koji šalje poruke (podatke)
> ![enter image description here](https://i.imgur.com/PaBPZez.jpg)
> Ako je root proces P0 koji ima niz podataka koji su podeljeni u grupe (sve iste veličine) označene sa A, B, C i D, ono što će svi procesi komunikatora (od P0 do poslednjeg procesa) dobiti su pojedinačni segmenti koji su iste veličine. 
>> Primer.
>> ```c
>> void main(int argc, char** argv)
>> {
>>      int rank, size, i; //dat je rank procesa, velicina komunikatora (broj procesa)
>>      double param[8], mine; 
>>      //param[8] je zapravo send bafer od 8 elemenata 
>>      //ako imamo 8 procesa, svaki proces ce da primi po 1 element niza param[8] 
>>      //i primice ga u svoju promenljivu mine
>>      int send_count, recv_count; 
>>      //send_count je broj podataka koji se salju svakom prosecu
>>      //recv_count je broj podataka koji se primaju u recv_buffer
>>      send_count = 1; //svakom procesu salje se po 1 element niza param
>>      recv_count = 1; //svaki prices dobice po 1 element
>>      MPI_Init(&argc, &argv);
>>      MPI_Comm_rank(MPI_COMM_WORLD, &rank);
>>      MPI_Comm_size(MPI_COMM_WORLD, &size);
>>      //proces sa rankom 3 je root, pa u njemu inicijalizujemo niz param[8]
>>      if(rank == 3)
>>      {
>>          for(i = 0; i<8; i++)
>>          {
>>              param[i] = 23 + i;
>>          }
>>      }
>>      MPI_Scatter(param, send_count, MPI_DOUBLE, &mine, recv_count, MPI_DOUBLE, 3, MPI_COMM_WORLD);
>>      //slanje pocinje od pocetka niza param, salje se po 1 element tipa MPI_DOUBLE i cuva se u promenljoj mine 
>>      //svakog procesa koji primi podatak, a mine je velicine 1 element tipa MPI_DOUBLE. Root proces je 3. 
>>      printf("P: %d, mine is %f\n", rank, mine);
>>      MPI_Finalize();
>>  }
>>  ```
>>  Rezultat izvršenja ove funkcije je:
>>  * P: 0 mine is 23.0
>>  * P: 1 mine is 24.0
>>  * P: 2 mine is 25.0
>>  * P: 3 mine is 26.0
>>  * ...
>>  * P: 7 mine is 30.0

> ### **MPI_Gather funkcija**
> [SUPROTNO OD MPI_Scatter!!!!]
> Omogućava da jedan proces formira sadržaj svog bafera kao skup podataka prikupljenih od ostalih procesa u datoj grupi. Tako je i-ti element tog bafera preuzet od i-tog procesa. 
> ```c
> int MPI_Gather(void* send_buffer, int send_count, MPI_Datatype send_type, void* recv_buffer, int recv_count, MPI_Datatype recv_type, int rank, MPI_Comm comm)
> ```
> Proces prima podatke i skladišti ih na osnovu identifikatora procesa (ranka) u toj grupi. Podaci iz send_buffer-a prvog člana grupe biće iskopirani u prvih recv_count lokacija bafera recv_buffer, podaci iz send_buffer-a drugog procesa u grupi biće iskopirani u sledećih recv_vount lokacija i tako redom.
> ![enter image description here](https://i.imgur.com/gkWdLv0.jpg)
>> Primer.
>> ```c
>> void main(int argc, char** argv)
>> {
>>     int rank, size;
>>     double param[16], mine; 
>>     //sada se podaci dobijaju u param, a mine je inicijalizovan za svaki proces i salje se
>>     int send_count, recv_count;
>>     int i;
>>     MPI_Init(&argc, &argv);
>>     MPI_Comm_rank(MPI_COMM_WORLS, &rank);
>>     MPI_Comm_size(MPI_COMM_WORLD, &size);
>>     send_count = 1; //svaki proces salje po 1 element iz svog send_buffer-a
>>     mine = 23 + rank;
>>     if(rank == 7) //7 je root proces
>>     {
>>         recv_count = 1; //prima se po 1 element od svakog procesa 
>>     }
>>     
>>     MPI_Gather(&mine, send_count, MPI_DOUBLE, param, recv_count, MPI_DOUBLE, 7, MPI_COMM_WORLD);
>>     // iz promenljive mine svakog procesa uzima se po 1 element tipa MPI_DOUBLE 
>>     // i redjaju se redom (promenljiva mine procesa P0, pa procesa P1, itd)u promenljivu param procesa P7 
>>     // mine procesa P0 -> P7: param[0]
>>     // mine procesa P1 -> P7: param[1]
>>     // mine procesa P2 -> P7: param[2]
>>     // param se generise kao skup mine iz svih procesa
>>     if(rank == 7)
>>     {
>>         for(i = 0; i<size; i++)
>>             printf("PE: %d, param[%d] is %f \n", rank, i, param);
>>     }
>>     MPI_Finalize();
>> }
>> ```
>> Izlaz za 10 procesa imaće sledeći izgled:
>> * PE: 7 param[0] = 23
>> * PE: 7 param[1] = 24
>> * ...
>> * PE: 7 param[9] = 32

# Grupne operacije - ZADACI
1. Napisati MPI program koji nalazi **_minimalnu_** i **_maximalnu_** vrednost zadate promenljive za N procesa kao i identifikatore procesa koji sadrže te vrednosti. 

_Kada se traži min i max, zajedno sa rankom procesa, koristi se Reduce sa MPI_MAXLOC/MPI_MINLOC koji daje id procesa koji sadrži max/min vrednost. U tom slučaju ulazni i izlazni podatak Reduce funkcije mora biti STRUKTURA! Da se ne traži i rank procesa, moglo bi bez strukture pomoću MPI_MAX/MPI_MIN. Tada se ne koristi MPI_DOUBLE_INT nego samo MPI_DOUBLE ili MPI_INT.
_
```c
#include <mpi.h>
void main(int argc, char** argv)
{
      int rank;
      struct { double value; int rank; }  in, out; //in je send buffer, out je recv buffer
      int root;
      MPI_Init(&argv, &argv);
      MPI_Comm_rank(MPI_COMM_WORLD, &rank);
      
      in.value = rank + 1;
      in.rank = rank;
      
      root = 5;
      
      MPI_Reduce(&in, &out, 1, MPI_DOUBLE_INT, MPI_MAXLOC, root, MPI_COMM_WORLD);
      //MPI_MAXLOC izvrsava se nad promenljivama in (koje su tipa MPI_DOUBLE_INT) svakog procesa
      //MPI_MAXLOC se moze izvrsavati SAMO NAD STRUKTURAMA PODATAKA, NE NAD STANDARDNIM TIPOVIMA PODATAKA
      //max vrednost svih in.value vrednosti je 7, a in.rank je tada 6
      //ova vrednost in bice zabelezena u procesu ciji je rang jednak root i to u promenljivoj out 
      //dakle u promenljivoj out procesa 5 bice zapamceno { value = 7, rank = 6 }
      if(rank == root)
      {
           printf("PE: %d min=%lf at rank %d\n", rank, out.value, out.rank);
      }
      
      MPI_Reduce(&in, &out, 1, MPI_DOUBLE_INT, MPI_MINLOC, root, MPI_COMM_WORLD);
      
      if(rank == root)
      {
           printf("PE: %d min=%lf at rank %d\n", rank, out.value, out.rank);
      }
      
      MPI_Finalize();
}
```

**_MPI_DOUBLE_INT_** je tip strukture koja ima 2 elementa tipa **INT** i **DOUBLE**. Postoji i **_MPI_FLOAT_INT_**, **_MPI_DOUBLE_INT_** i **_MPI_2INT_**. 

2. Svaki od N procesa sadrži 30 realnih brojeva. Napisati MPI program koji nalazi **max vrednost** na svakoj od 30 lokacija, kao i identifikator procesa koji sadrži tu vrednost. 

> ![enter image description here](https://i.imgur.com/gkWdLv0.jpg)

> U suštini, imamo niz in[30] i out[30], koji su tipa strukture MPI_INT_DOUBLE. To znači da svaki element niza in ima value i rank deo. Takođe, svaki proces ima posebnu vrednost za svaki svoj element niza in, kao na slici. 

```c
#include <stdion.h>
#include <mpi.h>
void main(int argc, char** argv)
{
     struct { double value; int rank; } in[30], out[30];
     int i, rank, size, send_count;
     //root je proces 0
     MPI_Init(&argc, &argv);
     MPI_Comm_rank(MPI_COMM_WORLD, &rank);
     MPI_Comm_size(MPI_COMM_WORLD, &size);
     
     for(i = 0; i < 30; i++)
     {
         in[i].value = double(rank + i);
         in[i].rank = rank;
     }
     
     MPI_Reduce(in, out, 30, MPI_DOUBLE_INT, MPI_MAXLOC, 0, MPI_COMM_WORLD);
     
     if(rank == 0)
     {
         for(i = 0; i < 30; i++)
         {
              printf("outvalue = %f", out[i].value);
              printf("outrank = %d", out[i].rank);
         }
     }
     MPI_Finalize();
}
```

3. Napisato MPI program koji izračunava vrednost broja PI kao vrednost integrala funkcije  f(x) = 4 / (1 + x^2) na intervalu [0,1].

> ![enter image description here](https://i.imgur.com/Kkhzhl6.jpg)

> Interval funkcije od 0 do 1 podeljen je na **N** delova i što je veće N, to je veća preciznost. Svaki mali deo (pravougaonik) nazivamo **segment** i označavamo sa **delta x**. Širina segmenta označava se sa **h** i iznosi 1/N. Površina svakog pravouganika/segmenta nalazi se tako što se nađe vrednost funkcije F(xi) u tački xi na sredini tog pravougaonika/segmenta. Svaki proces će vršiti deo izračunavanja, računa površine određenih pravougaonika. Ceo integral se dobija zbirom svih površina pravougaonika. Procesu P0 dodeli se površina prvog pravougaonika, procesu P1 sledeći itd. Procesu P0 se opet dodeli neki pravougaonik, kada se na njega opet ciklično dođe na red. U slučaju da imamo N=10, a da je broj procesa 5, onda svaki proces računa površinu za N/5 = 2 pravouganika.

> ![enter image description here](https://i.imgur.com/NqN66MU.jpg)

Pošto će svaki proces da računa po 2 površine, može se odrediti način da procesi to rade paralelno. P0 će nalaziti vrednost funkcije u tački x0 = 1/20 što je zapravo x0 = 0.5 * h. P0 će kasnije izračunavati površinu i za pravougaonik u intervalu 5/10 do 6/10, pa je x5 = 5,5 * h = 5,5 / 10. Od ćega zavisi u kojoj tački svaki proces računa prvo izračunavanje i sledeće izračunavanje? Zavisi od ranka i broja procesa. Sledeću tačku definiše broj procesa.

Pa ako imamo **N - broj pravougaonika (segmenata)** i imamo **P - broj procesa**, pa logično je da će svaki proces da izračuna površine K = N/P pravougaonika (ako su deljivi N sa P). I onda za K = 0 imamo P0, P1, ... Pp procesa za prvih P pravougaonika, pa za K = 1 opet P0, P1, ... Pp za ostalih P pravougaonika itd. 

```c
void main(int argc, char** argv)
{
      int N, rank, P, i; //N je broj pravougaonika, P je broj procesa 
      double realPI = 3.14159;
      double mypi, pi, h, fx, xi; // h je sirina segmenta tj. 1/N
      
      MPI_Init(&argc, &argv);
      MPI_Comm_size(MPI_COMM_WORLD, &P);
      MPI_Comm_rank(MPI_COMM_WORLD, &rank);
      
      if(rank == 0)
      {
            printf("Unesi broj segmenata: ");
            scanf("%d", &N);
      }
      
      MPI_BCast(&N, 1, MPI_INT, 0, MPI_COMM_WORLD); //svi procesi za promenljivu N treba da imaju tu vrednost
      
      h = (double) (1.0 / (double) N); // h = 1/N
      fx = 0.0; // vrednost funkcije u tacki xi 
      
      for(i = rank;i < N;i += P)
      {
          xi = h * ((double)i + 0.5); // xi = h * (brPravougaonika + 0.5);
          fx += 4.0 / (1.0 + xi * xi); //f(xi) += 4 / (1 + xi^2) ali za oba pravougaonika po procesu! Zato je += 
          //za P0 bice fxi = 4/(1 + h*0.5*0.5) + 4/(1 + 5.5 * 5.5 * h)
      }
      mypi = h * fx; //povrsinaPravougaonika = h * f(xi)
      
      MPI_Reduce(&mypi, &pi, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);
      //uzeti promenljive mypi svakog procesa i sabrati ih u promenljivu p procesa 0 
      
      if(rank == 0)
      {
           //ako je proces P0 onda on ima rezultat pi, pa moze da se stampa
           printf("pi je oko %.16f, greska je %.16f", pi, fabs(pi - realPI));
      }
      
      MPI_Finalize();
}
```
4. Napisati MPI program koji izračunava vrednot skalarnog proizvoda dva vektora dimenzije N. Pretpostaviti da je N deljivo sa brojem procesa P. Vrednosti vektora a i b se učitavaju u procesu P0. 

```c
#include <mpi.h>
#include <stdio.h>
#define N 6

void main(int argc, char** argv)
{
       float a[N], b[N], dot, local_dot = 0;
       int i, n_bar, my_rank, P;
       
       MPI_Init(&argc, &argv);
       MPI_Comm_size(MPI_COMM_WORLD, &p);
       MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
       
       n_bar = N / P; //koliko pravougaonika ce svaki proces obraditi
       //pravougaonik ce biti deo vektora A i deo vektora B
       
       float* local_a = (float*)malloc(n_bar * sizeof(float));
       //svaki proces ima local_a niz u kome se nalaze elementi niza a za taj proces, dakle N/P ih ima u tom nizu
       float* local_b = (float*)malloc(n_bar * sizeof(float));
       
       if(my_rank == 0)
       {
            for(i = 0; i < N; i++)
            {
                //ucitaj niz a u procesu P0
                scanf("%f", &a[i]);
            }
       }
       
       MPI_Scatter(a, n_bar, MPI_FLOAT, local_a, n_bar, MPI_FLOAT, 0, MPI_COMM_WORLD);
       //na ovaj nacin proces P0 iz niza a n_bar elemenata tipa MPI_FLOAT prosledjuje ostalim procesima (a i sebi)
       //tj. njihovim local_a nizovima, koji pamti elemente tipa MPI_FLOAT
       
       if(my_rank == 0)
       {
            for(i = 0; i < n; i++)
            {
                 //ucitaj niz b u procesu P0
                 scanf("%f", &b[i]);
            }
       }
       
       MPI_Scatter(b, n_bar, MPI_FLOAT, local_b, n_bar, MPI_FLOAT, 0, MPI_COMM_WORLD);
       //na ovaj nacin proces P0 iz niza b n_bar elemenata tipa MPI_FLOAT prosledjuje ostalim procesima 
       //tj. njihovim local_b nizovima, koji pamti elemente tipa MPI_FLOAT
       
       for(i = 0; i < n_bar; i++)
             local_dot += local_a[i] * local_b[i];
       
       MPI_Reduce(&local_dot, &dot, 1, MPI_FLOAT, MPI_SUM, 0, MPI_COMM_WORLD);
       
       if(my_rank == 0)
             printf("The dot producet is %f\n", dot);
       
       MPI_Finalize();
}
```

5. Napisati MPI program koji pronalazi **proizvod** _matrice Anxn_ i _vektora bn_. Matrica A i vektor b se inicijalizuju u procesu 0. Izračunavanje se obavlja tako što se svakom procesu distribuira po vrsta matrice A i ceo vektor b. Svi procesi učestvuju u izračunavanju. Rezultat se prikazuje u procesu 0. 

> ![enter image description here](https://i.imgur.com/8aCbXIa.jpg)

```c
#include <stdio.h>
#include <mpi.h>
#define N 6

void main(int argc, char** argv)
{
      int rank, P, i, j, a[N][N], b[N], local_a[N], local_c, c[N];
      
      MPI_Init(&argc, &argv);
      MPI_Comm_rank(MPI_COMM_WORLD, &rank);
      MPI_Comm_size(MPI_COMM_WORLD, &P);
      
      if(rank == 0)
      {
           //u procesu P0 se inicijalizuje a i b
           for(i = 0; i < N; i++)
           {
                 for(j = 0; j < N; j++)
                 {
                      a[i][j] = i + j;
                 }
                 
                 b[i] = 1;
           }
      }
      
      //svaki proces prima jednu vrstu matrice A:
      MPI_Scatter(&a[0][0], N, MPI_INT, local_a, N, MPI_INT, 0, MPI_COMM_WORLD);
      //proces P0 od pocetka matrice a salje N podataka tipa MPI_INT ka svim ostalim procesima ukljucujuci i P0
      //i ti podaci smestaju se u promenljivu local_a svakog procesa koja prima N podataka tipa MPI_INT 
      
      //svaki proces prima ceo niz B:
      MPI_Bcast(b, N, MPI_INT, 0, MPI_COMM_WORLD);
      //proces P0 niz b od N elemenata tipa MPI_INT salje od ka svim ostalim procesima, ukljucujuci u P0
      
      local_c = 0;
      for(i = 0; i < N; i++)
      {
            local_c += local_a[i] * b[i];
      }
      
      //Rezultat je u procesu P0:
      MPI_Gather(&local_c, 1, MPI_INT, &c[0], 1, MPI_INT, 0, MPI_COMM_WORLD);
      //sada iz promenljive local_c svakog procesa uzimamo po 1 element tipa MPI_INT i smestamo ga u promenljivu c
      //procesa P0, na kraju ce u promenjivoj c biti ceo vektor koji predstavlja kompletan proizvod A * b
      
      if(rank == 0)
      {
            printf("c= [");
            for(i = 0; i < N; i++)
                  printf("%d ", c[i]);
            pritnf("]\n");
      }
}
```

6. Napisati MPI program koji pronalazi i prikazuje minimalni neparan broj sa zadatom osobinom i identifikator procesa koji ga sadrži. Neparni brojevi se nalaze u intervalu [a,b] (a i b su zadate kontante). Osobina koju broj treba da poseduje je da je deljiv zadatom vrednošću x. Prilikom ispitivanja (da li broj poseduje datu osobinu ili ne) svaki proces generiše i ispituje odgovarajuće neparne brojeve način prikazan na slici. Za primer broj_proces = 4 i a = 3, b = 31, x = 5. Konačne rezultate treba da prikaže proces koji sadrži najmanji broj takvih brojeva. Zadatak rešiti korišćenjem grupnih operacija. 

> ![enter image description here](https://i.imgur.com/HTAOVul.jpg)

```c
#include <mpi.h>
#include <stdio.h>
#define a 5
#define b 31
#define x 5

void main(int argc, char** argv)
{
     struct { int value; int rank; } f, c, d, e;
     int my_rank, p, b1 = 0, z, min = INT_MAX;
     
     MPI_Init(&argc, &argv);
     MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
     MPI_Comm_size(MPI_COMM_WORLD, &p);
     
     for(z = a + 2*my_rank; z <= b; z+= p*2)
     {
            if(z%x == 0)
            {
                   b1++;
                   if(z<min) 
                       min = z;
            }
     }
     
     c.value = min;
     c.rank = my_rank;
     d.value = b1;
     d.rank = my_rank;
     
     MPI_Reduce(&d, &e, 1, MPI_2INT, MPI_MINLOC, 0, MPI_COMM_WORLD);
     MPI_Bcast(&e, 1, MPI_2INT, 0, MPI_COMM_WORLD);
     MPI_Reduce(&c, &f, 1, MPI_2INT, MPI_MINLOC, e.rank, MPI_COMM_WORLD);
     
     if(my_rank == e.rank)
          printf("%d %d", f.value, f.rank);
     
     MPI_Finalize();
}
```

7. Napisati MPI program koji pronalazi  proizvod matrice Amxn i vektora bn. Matrica A i vektor b se inicijalizuju u procesu 0. Izračunavanje se obavlja tako što se svakom procesu distribuira po kolona matrice A i po 1 element vektora b. Za distribuciju kolona po procesima koristiti Point-to-point operacije, za sve ostalo grupne operacije. Svi procesi učestvuju u izračnavanju. Rezultat se prikazue u procesu koji, nakon distribuiranja kolona matrice A, sadrži minimum svih elemenata matrice A. 

> ![enter image description here](https://i.imgur.com/6LdmemU.jpg)

```c
#include <mpi.h>
#define m 4
#define n 3

void main(int argc, char** argv)
{
       int a[m][n], b[n], rank, p, i, j;
       int lc[m], c[m], y[m], x[m], z;
       struct { int valuel int rank; } min, gmin;
       
       MPI_Init(&argc, &argv);
       MPI_Comm_rank(MPI_COMM_WORLD, &rank);
       MPI_Comm_size(MPI_COMM_WORLD, &p);
       
       if(rank == 0)
       {
            for(i = 0; i < M; i++)
            {
                   for(j = 0; j < N ;j++)
                   {
                          a[i][j] = i + j;
                   }
            }
            
            for(i = 0; i < N; i++)
            {
                  b[j] = 1;
            }
       }
       
       if(rank == 0)
       {
            for(i = 0; i < m; i++)
                  x[i] = a[i][0];

            for(j = 1; j < p; j++)
            {
                 for(i = 0; i < m; i++)
                 {
                       y[i] = a[i][j];
                 }
                 MPI_Send(y, m, MPI_INT< j, 0, MPI_COMM_WORLD);
            }
       }
       else 
       {
            MPI_Recv(x, m, MPI_INT, 0, 0, MPI_COMM_WORLD, &status);
       }
       
       MPI_Scatter(&b[0], 1, MPI_INT< &z, 1, MPI_INT, 0, MPI_COMM_WORLD);
       
       for(i = 0; i < m; i++)
              lc[i] = x[i] * z;
       
       min.value = INT_MAX;
       for(i = 0; i < m; i++)
       {
            if(x[i] < min.value)
            {
                 min.value = x[i];
                 min.rank = rank;
            }
       }
       
       MPI_Reduce(&min, &gmin, 1, MPI_2INT, MPI_MINLOC, 0, MPI_COMM_WORLD);
       MPI_Bcast(&gmin, 1, MPI_2INT, 0, MPI_COMM_WORLD);
       MPI_Reduce(lc, c, m, MPI_INT, MPI_SUM, gmin.rank, MPI_COMM_WORLD);
       
       if(rank == gmin.rank)
       {
            for(i = 0; i < m; i++)
            {
                  printf("c[%d] = %d\n", i, c[i]);
                  printf("gmin = %d", gmin.value);
            }
       }
       
       MPI_Finalize();
}
```
