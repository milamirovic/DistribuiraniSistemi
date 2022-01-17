# RMI zadaci

Kratak opis kako se rade zadaci iz ove oblasti.
Npr. ako imamo primer Hotela i rezervacije soba, objekat tipa Soba bi bio objekat cije se stanje menja, klijent vrsi izmenu njegovog stanja, pa je zato taj objekat remote. Znaci morali bi da imamo interfejs Soba (Remote) i klasu SobaImpl (UnicastRemoteObject). 
Ako imamo i objekat koji predstavlja putnika, njegovo stanje se ne menja, pa je on obican objekat. Obicni objekti MORAJU da implementiraju Serializable!





## Osnovne klase/interfejsi koje moraju da postoje 

Svaki RMI projekat ima sledece klase/interfejse:

* Server.java
* Client.java
* Manager.java - interfejs
* ManagerImpl.java - klasa 
* Callback.java - interfejs

Uglavnom postoji jos neka dodatna klasa, sto zavisi od teksta zadatka. 


## Server.java

U ovoj klasi mora da se kreira RMI registry pomocu:


```javascript
LocateRegistry.createRegistry(portNumber);
```

Moramo da instancitamo remote objekat (to je uvek objekat tipa Manager). Taj objekat moramo da registrujemo u RMI registry i to radimo na sledeci nacin: 

```javascript
Manager manager = new ManagerImpl();
Naming.rebind("rmi://" + host + ":" + port + "/" + service, manager);
```
Ja obicno stavljam sve to u konstruktor klase Servera. U samoj klasi kao atribut mi je manager. 

## Client.java

* Atributi klase Client su manager i callback (callback nije uvek neophodan kao atribut)
* Vrsimo pretrazivanje RMI registry-a i pribavljamo interfejs remote objekta - manager
* U okviru klase Client moramo da definisemo novu klasu CallbackImpl 

Dakle, atributi klase Client:
```javascript
private Manager manager;
private Callback callback; //opciono
```
Konstruktor Client-a:
```javascript
this.manager = (Manager)Naming.Lookup("rmi://" + host + ":" + port + "/" + service);
this.callback = new CallbackImpl();
```
U okviru tela klase Client definišemo novu klasu - CallbackImpl koja implementira Callback interfejs:
```javascript
public class CallbackImpl extends UnicastRemoteObject implements Callback 
{
    public void callbacMetoda(atributi) throws RemoteException
    {
        //implementacija callback metode je obicno stampanje neke poruke
    }
}
```

## Manager.java i ManagerImpl.java

Ono sto je bitno znati je da kada imamo remote objekat onda obavezno imamo interfejs i klasu koja nasledjuje taj interfejs. Interfejs obavezno nasledjuje java.rmi.Remote, a klasa implementira interfejs i nasledjuje java.rmi.UnicastRemoteObject!

Interfejs (kao i uvek) ima samo potpise metoda koje klasa treba da implementira. Koje funkcije ce biti definisane zavisi od teksta zadatka, one su uvek detaljno opisane u zadatku. 

U klasi ManagerImpl mora da postoji lista svih objekata od znacaja u tom zadatku. U primeru bele table, manager bi imao listu svih oblika koji se na tabli nalaze. U zadatku je obicno naglaseno sta sve ima ova klasa. 
Ako je zadatak sa taxijima u pitanju, manager bi imao atribut listu svih taksija npr. Manager je najbitnija klasa. 

Ako je zadatak u kome treba da se pamti lista Vlasnika i njihova vozila, to bi bila neka HashMap-a gde je key npr id Vlasnika, a value je Vozilo. 

## Callback.java i CallbackImpl.java

Callback.java je **interface** (nasledjuje Remote) koji ima potpis uglavnom jedne ili dve funkcije koje su zapravo callback metode. Sta rade te callback funkcije? Naravno, lepse je objasnjeno na samim vezbama. Callback metode su zaduzene da obaveste klijente o odredjenim izmenama/desavanjima i celom sistemu. Npr. ako imamo primer bele table na kojoj moze da crta vise klijenata, kada jedan klijenat nacrta jedan oblik, njega treba da vide i ostali klijenti, ne samo taj koji je nacrtao. To se zove konzistentnost (sa predavanja). Dakle, mora da postoji neki metod koji ce da obavesti ostale klijente da je doslo do odredjenih izmena  u sistemu. Taj metod je callback metod. Svaki put kada nastane neko desavanje od znacaja, mora da se pozove metod callback koji ce da obavesti i ostale ucesnike. Nadam se da je ovo dovoljno jasno. 
CallbackImpl.java je klasa koja je definisana u okviru Client.java klase. Ona naravno implementira Callback interfejs i nasledjuje UnicastRemoteObject. 


## Ostale klase

Za ostale klase u zadatku uglavnom nije receno da li se radi o remote objektima ili standardnim objektima. Ako se u sistemu menja STANJE nekog objekta od tih dodatnih klasa
