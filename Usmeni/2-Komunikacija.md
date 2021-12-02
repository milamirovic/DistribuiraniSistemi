# Komunikacija

## Kako funkcioniše RPC?

Osnovna ideja RPC-a je da poziv udaljene procedure izgleda što sličnije pozivu lokalne (konvencionalne) procedure. Zato RPC obezbeđuje infrastrukturu neophodnu za transformisanje poziva procedure u poziv udaljene procedure na uniforman način, jer proces koji poziva udaljenu proceduru ne sme da bude svestan da se pozvana procedura izvršava na nekoj drugoj mašini. 

1. Poziv konvencionalne procedure:
   - nakon poziva procedure upravljanje se prenosi na pozvanu proceduru 
   - na kraju izvršenja, pozvana procedura vraća upravljanje glavnom programu
   - i glavni program i pozvana procedura izvršavaju se na istoj mašini

2. Poziv udaljene procedure:
   - poziva se udaljena procedura na klijentu, pa se zahtev salje serveru koja se tamo izvršava
   - nakon izvršenja udaljene procedure na serveru, ona šalje rešenje u vidu odgovora na zahtev i to stiže do klijenta koji koristi dobijenu vrednost

Prenos parametara se može obaviti:
- **call-by-value (po vrednosti)** - vrednosti parametara se kopiraju u stek. Tako da ako pozvana procedura modifikuje prosleđene parametre, originalni podaci se ne modifikuju.
- **call-by-reference (po referenci)** - u stek se upisuje adresa parametra, a ne vrednost. Tako da ako pozvana procedura modifikuje preneti parametar, ona modifikuje originalnu vrednost na strani primaoca, a ne kopiju.
- **call-by-copy/restore** - kod poziva procedure parametri se kopiraju u stek kao kod poziva po vrednosti, a ankon okončanja poziva vrednosti se upisuju preko originalnih vrednosti parametara, kao kod poziva po referenci.
![alt txt] (https://i.imgur.com/sYg2UFC.jpg)

Kod RPC-a prenos parametara vrši se kao prenos po vrednosti. 

## Koraci kod poziva RPC-a:
1. Klijent poziva lokalnu proceduru (**klijent stub**) i parametri se smeštaju u stek.
2. Klijent stub pakuje argumente za udaljenu proceduru i gradi jednu ili više mrežnih poruka i poziva lokalni OS (poziva SEND primitivu, a nakon toga i RECEIVE primitivu i čeka se dok ne stigne odgovor).
3. Lokalni OS šalje poruku udaljenom OS-u pomoću transportnog protokola - TCP ili UDP. 
4. Prijem poruke - mrežna poruka stiže do odredišta, OS prosleđuje poruku serverskom stub-u i on prima poruku od lokalnog OS-a (izvršava RECEIVE primitivu), raspakuje je i izvlači argumente za poziv lokalne procedure. 
5. Serverski stub poziva željenu serversku proceduru i predaje joj parametre koje je primio od klijenta tako što ih stavlja u stek. 
6. Server izvršava pozvanu proceduru i vraća rezultat stub-u.
7. Serverski stub pakuje rezultat u poruku i poziva svoj lokalni OS (poziva SEND primitivu, pa nakon toga RECEIVE primitivu i čeka na sledeći zahtev).
8. Slanje poruke kroz mrežu - serverski OS šalje poruku klijentskom OS-u.
9. Lokalni OS prosleđuje poruku klijent stub-u.
10. Klijent stub izvlači rezultat iz poruke i vraća klijentskom procesu

Konkretan primer:
- Neka je add(i,j) udaljena procedura koja ima 2 parametra tipa int i kao rezultat vraća zbir ta dva int-a. 
- Poziv udaljene add f-je će prihvatiti klijent stub. Klijent stub uzima parametre i stavlja ih u poruku. U poruku stavlja ime ili broj procedure koja se poziva.
- Kada poruka stigne do servera, serverski stub ispituje poruku da bi ustanovio koja procedura se poziva i zatim upućuje odgovarajući poziv.
- Kada se procedura izvrši, serverski stub ponov preuzima kontrolu. Prihvata rezultat izvršenja procedure, pakuje ga u poruku, šalje poruku klijentu. 
- Klijenstki stub raspakuje poruku i šalje rezultat klijentskoj proceduri.

## Kako locirati udaljeni server i proceduru?
Postoje 2 moguća rešenja:
1. Statičko povezivanje
   - Klijent zna koji host treba da kontaktira (njegova adresa je u klijent stub-u), a kada klijent pozove odgovarajuću proceduru, klijent stub jednostavno prosledi poziv serveru. 
   - Prednosti ovog pristupa: nema potrebe za dodatnom infrastrukturom osim klijetn i server stub-a
   - Nedostaci: klijent i server su čvrsto povezani (ako server otkaže, klijent neće raditi). Ako server promeni lokaciju (IP adresu) klijent mora da se rekompilira sa novim stub-om koji ukazuje na pravu lokaciju.

2. Dinamičko povezivanje
   - Postoji centralizovana baza podataka, smeštena u name i directory serverima, koja može locirati host koji obezbeđuje željeni servis. Ovi name i directory serveri vraćaju adresu servera na osnovu potpisa procedure (imena i parametara) koja se poziva. Kada klijent pozove udaljenu proceduru, klijentski stub kontaktira name server i dobija adresu servera koji tu proceduru izvršava. Name server šalje adresu klijent stub-u koji zatim uspostavlja vezu sa željenim serverom.


## Semantika poziva udaljenih procedura 
Udaljena procedura će se izvršiti tačno jednom kada je pozvana, ali može se izvršiti:
- 0 puta ako server otkaže pre izvršenja
- jednom ako je sve okej
- 1 ili više puta, ako je komunikaciono kašnjenje veliko ili se izgubi odgovor od strane servera pa nastane retransmisija

Većina RPC sistetma obezbeđuje:
- "bar jednom" semantiku - ako su u pitanju funkcije koje se mogu izvršavati bez posledica više puta, kao što je čitanje podataka, prikaz vremena, datuma itd.
- "samo jednom" semantiku - ako funkcija izaziva posledice, kao što je modifikovanje fajla

# Sun RPC
Obezbeđuje jezik za definisanje interfejsa - XDR (eXternal Data Representation) i interfejs kompajler (rpcgen) koji se koristi za generisanje klijent i server stub funkcija. 
rpcgen kompajler generiše 3 fajla:
- header fajl - sadrži id interfejsa, definiciju tipova, konstanti i prototipova funkcija
- klijent stub - sadrži procedure koje će klijent program pozivati
- server stub - sadrži procedure koje se pozivaju kada stigne poruka do servera i koje zatim pozivaju odgovarajuću serversku proceduru

## Definisanje interfejsa za pristup udaljenom servisu SABOD 
Sve udaljene procedure se deklarišu kao deo udaljenog programa. Imena programa, ime verzije, imena procedura u IDL fajlu se po pravilu pišu velikim slovima. Udaljene procedure koje su definisane pomoću IDL se u klijent programu pozivaju malim slovima nadodavanjem imena procedure iza kojeg sledi donja crtica i broj verzije. (**imeprocedure_brojverzije**). Ove procedure u server programu imaju ime (**imeprocedure_brojverzije_svc**).

Nakon kompajliranja IDL fajla komandom **rpcgen -C primer.x**, dobijaju se sledeći fajlovi:
- primer.h (**header**)
- primer_clnt.c (**klijent stub**)
- primer_svc.c (**server stub**)

Primer: servis KALK za sabiranje i oduzimanje dva cela broja, ima dve procedure: SABERI(x,y) i ODUZMI(x,y). Pošto SunRPC poziv udaljene procedure može imati samo jedan argument, to ćemo srediti ovako:

```java
struct operandi 
{
    int x;
    int y;
}

program KALK 
{
    version KALK_VERSION
    {
        int SABERI(operandi) = 1;
        int ODUZMI(operandi) = 2;
    } = 1
} = 0x2999999
```

Svaka RPC procedura je definisana u okviru nekog programa i identifikovana je na jedinstven način:
- **brojem programa** - (ASCI string u HEX notaciji) - identifikuje grupu udaljenih procedura, od kojih svaka ima svoj broj
- **brojem verzije** - ako se napravi izmena u udaljenom servisu, programu se dodaje novi broj verzije
- **brojem procedure** - numeracija kreće od 1

```java
PROGRAM ime_programa
{
    def_verzije
    def_verzije
} = 0xYYYYYYYY - 32 bit identifikator

VERSION ime_verzije
{
    def_procedure1
    def_procedure2
    ...
} = konstanta

definicija procedure
    tip ime_procedure(tip_argumenta) = konstanta;
```
Klijent strana:
```java
#include <stdio.h>
#include <rpc/rpc.h> //standardna biblioteka za rpc
#include "primer.h" //fajl generisan pomovu rpcgen
int main(int argc, char *argv[])
{
    CLIENT *cln;
    char *server;
    int *zbir, *razlika;//povratne vrednosti iz udaljenih procedura  
    operandi op; //struktura definisana u IDL fajlu
    if(argc<2)
    {
        exit(1);
    }
    server = argv[1];
    cln = clnt_create(server, KALK, KALK_VERSION, "udp");
    //kreira se socket i klijent handle i povezuje sa serverom
    if(cln==NULL)
    {
        //konekcija se serverom nije uspostavljena
        exit(2);
    }
    op.x = atoi(argv[2]);
    op.y = atoi(argv[3]);
    
    zbir = saberi_1(&op, cln);//poziv udaljene procedure - poziva se stub fja
    if(zbir == (int*)NULL)
    {
        exit(3);
    }
    printf("suma je %d\n", *zbir);
    
    razlika = oduzmi_1(&op, cln);
    //poziv udaljene procedure, argument procedure je ptr na argument ciji je tip definisan u IDL fajlu. Procedura mora da vrati ptr na rezultat
    if(razlika == (int*)NULL)
    {
        exit(4);
    }
    printf("razlika je %d\n", *razlika);
    clnt_destroy(cln);
    exit(0);
}
```

Pomoću rpcgen može se generisati template za serverski kod korišćenjem prethodno definisanog interfejsa (primer.x).
> rpcgen -C -Ss primer.x > server.c 
> > server.c je ima fajla u kome je zapamcen serverski kod
> rpcgen generise sledeci kod:
> ```java
> #include primer.h
> int *saberi_1_svc(operandi *argp, struct svc_req *rqstp)
> {
>       static int result;
>       //serverski kod  
>       return &result;
> }
> int *oduzmi_1_svc(operandi *argp, struct_svc_req *rqstp)
> {
>       static int result;
>       //serverski kod 
>       return &result;
> }

Server strana:
```java
int *saberi_1_svc(operandi *a, struct svc_req *rqstp)
{
    static int zbir;
    zbir = a->x + a->y;
    return &zbir;
}

int *oduzmi_1_svc(operandi *a, struct svc_req *rqstp)
{
    static int razlika;
    razlika = a->x - a->y;
    return &razlika;
}
```

Klijent mora da zna ime udaljenog servera. Server registruje svoje usluge preko portmapper deamon procesa na serverskoj mašini (na portu 111). 
- generisanje stub funkcija: *rpcgen -C primer.x*
- kompajliranje i linkovanje klijenta i klijent stub-a: *cc -o klijent klijent.c primer_clnt.c*
- kompajliranje i linkovanje servera i server stub-a: *cc -o server server.c primer_svc.c*
- izvršenje
- pozvati serversko program na izvršenje: ./server
- pozvati klijent program: ./klijent -h remus 12 15
