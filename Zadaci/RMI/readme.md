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

Moramo da instancitamo remote objekat (to je uvek objekat tipa Manager, a u nekim slucajevima moze biti i objekat jos neke klase). Taj objekat moramo da registrujemo u RMI registry. 

```javascript
Manager manager = new ManagerImpl();
Naming.rebind("rmi://" + host + ":" + port + "/" + service, manager);
```


## Client.java

* Atributi klase Client su manager i callback 
* Vrsimo pretrazivanje RMI registry-a i pribavljamo interfejs remote objekta - manager
* U okviru klase Client moramo da definisemo novu klasu CallbackImpl 

```javascript
private Manager manager;
private Callback callback;
```

```javascript
this.manager = (Manager)Naming.Lookup("rmi://" + host + ":" + port + "/" + service);
this.callback = new CallbackImpl();
```

```javascript
public class CallbackImpl extends UnicastRemoteObject implements Callback 
{
    ...
}
```

## Manager.java i ManagerImpl.java

Ono sto je bitno znati je da kada imamo remote objekat onda obavezno imamo interfejs i klasu koja nasledjuje taj interfejs. Interfejs obavezno nasledjuje java.rmi.Remote, a klasa implementira interfejs i nasledjuje java.rmi.UnicastRemoteObject!

Interfejs (kao i uvek) ima samo potpise metoda koje klasa treba da implementira. Koje funkcije ce biti u definisane zavisi od teksta zadatka, one su uvek detaljno opisane u zadatku. 

U klasi ManagerImpl mora da postoji lista svih objekata od znacaja u tom zadatku. U primeru bele table, manager bi imao listu svih oblika koji se na tabli nalaze. U zadatku je obicno naglaseno sta sve ima ova klasa. 


## Callback.java i CallbackImpl.java

Callback.java je interface (nasledjuje Remote) koji ima potpis uglavnom jedne ili dve funkcije koje su zapravo callback funkcije. Sta rade te callback funkcije? Naravno, lepse je objasnjeno na samim vezbama. Callback metode su zaduzene da obaveste klijente o odredjenim izmenama/desavanjima i celom sistemu. Npr. ako imamo primer bele table na kojoj moze da crta vise klijenata, kada jedan klijenat nacrta jedan oblik, njega treba da vide i ostali klijenti, ne samo taj koji je nacrtao. Dakle, mora da postoji neki metod koji ce da obavesti ostale klijenta da je doslo do odredjenih izmena. Taj metod je callback metod. Svaki put kada nastane neko desavanje od znacaja, mora da se pozove metod callback koji ce da obavesti i ostale ucesnike. Nadam se da je ovo dovoljno jasno. 
CallbackImpl.java je klasa koja je definisana u okviru Client.java klase. Ona naravno implementira Callback interfejs i nasledjuje UnicastRemoteObject. 


## Ostale klase

Za ostale klase u zadatku uglavnom nije receno da li se radi o remote objektima ili standardnim objektima. Ako se u sistemu menja STANJE nekog objekta od tih dodatnih klasa
