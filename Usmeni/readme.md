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
