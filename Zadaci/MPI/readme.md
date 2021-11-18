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
> Isti proces može imati različite vrednosti za rank u različitim komunikatorima. 

### MPI_Comm_size funkcija
```
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

# Point-to-point komunikacija BEZ deadlock-a (uzajamnog isključivanja)
```
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

```
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

```
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
```
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

```
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
```
int MPI_Wait(MPI_Request * request, MPI_Status *status)
```
> Proces se iz ove funkcije vraća onda kada se operacija identifikovana sa **request** izvrši. Ako je inicirana operacija *MPI_Irecv()*, onda ***MPI_Status status*** čuva informaciju o izvoru poruke, oznaci poruke - tag, kao i broju primljenih podataka. U slučaju *MPI_Isend()*, status čuva informaciju o grešci. Ova operacija je blokirajuća. 

## MPI_Test(MPI_Request *request, int *flag, MPI_Status *status)

Funkcija MPI_Test vraća informaciju o trenutnom stanju operacije koja je identifikovana argumentom request:

```
int MPI_Test(MPI_Request *request, int *flag, MPI_Status *status);
```

Argument **flag** je ***true*** ako je operacija završenja, u suprotnom je ***false***. Argument **status** sadrži dodatne statusne informacije. Ova operacija nije blokirajuća. 

### Primer

```
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

***Operacija se izvršava kada svi procesi pozovu odgovarajuću operaciju sa svojim parametrima. Svaki proces mora da pozove grupnu operaciju da bi se ona obavila. ***

Grupne operacije dele se na:
* **operacije za kontrolu procesa**, 
* **operacije za globalna izračunavanja**
* **operacija za prenos podataka**

> ## Operacije za kontrolu procesa 
>> ### MPI_Barrier funkcija
>> ```
>> int MPI_Barrier (MPI_Comm comm);
>> ```
>>> **Funkcija MPI_Barrier implementira sinhronizacioni mehanizam poznat kao *barijera*. Proces se blokira na toj naredbi dok svi ostali procesi iz grupe ne dođu do te naredbe. Tada se svi procesi vraćaju daljem izvršenju.**
>>> ![enter image description here](https://i.imgur.com/dV8x7U3.jpg)

> ## Operacije za globalna izračunavanja
> Ovde spadaju *operacije za redukciju podataka* i *operacija scan*. Operacija redukcije uzima podatke u ulaznim baferima svih procesa, primenjuje nad njima datu operaciju redukcije i smešta rezultat u root procesa. 
>> ### MPI_Reduce funkcija
>> ```
>> int MPI_Reduce(void * send_buffer, void * recv_buffer, int count, MPI_Datatype dtype, MPI_Op operation, int rank, MPI_Comm comm);
>> ```
>>> **send_buffer** je adresa bafera svih procesa gde se nalaze podaci nad kojima se obavlja operacija redukcije. Svaki proces će da ima u grupi svoj send_buffer. Reduce operacija će uzeti podatke u ulaznim baferima svih procesa i nad njima vrši redukciju pomoću operacije operation. 
>>> **recv_buffer** je adresa bafera root procesa. U njega Reduce funkcija smešta rezultat. To je bafer samo jednog procesa (čiji je rank naveden kao argument funkcije), ne svih procesa! 
>>> **count** je broj podataka u send i receive baferu nad kojima se obavlja operacija redukcije
>>> **dtype** je tip podataka u send i receive baferu
>>> **comm** je komunikator
>>> **operation** - operacija redukcije je definisana tim argumentom
>>> **rank** je identifikator root procesa

>> Operation može da ima sledeće vrednosti:
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
>>> ```
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
