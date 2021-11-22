# Point-to-point funkcije sa blokiranjem
> Proces određuje svoj rank u komunikatoru korišćenjem funkcije:
> ```c
> int MPI_Comm_rank(MPI_Comm comm, int* rank)
> ```

> Veličina komunikatora dobija se funkcijom:
> ```c
> int MPI_Comm_size(MPI_Comm comm, int* size)
> ```

> Funkcija kojom se vrši slanje sa blokiranjem:
> ```c
> int MPI_Send(void* buf, int count, MPI_Datatype dtype, int dest, int tag, MPI_Comm comm)
> ```
> **Buf** ukazuje na mesto u memoriji odakle počinje slanje **count** podataka tipa **dtype**, a šalje se procesu sa rankom **dest**. 
> * **buf** - mesto u memoriji odakle se počinje sa slanjem podataka
> * **count** - broj podataka koji se šalju
> * **dtype** - tip podataka koji se šalju
> * **dest** - rank procesa koji prima podatke
> * **tag** - oznaka poruke (mora da se poklapa u Send i Recv)
> * **comm** - komunikator u kome se obavlja komunikacija

> Funkcija koja vrši primanje podataka
>  ```c
>  int MPI_Recv(void* buf, int count, MPI_Datatype dtype, int source, int tag, MPI_Comm comm, MPI_Status* status)
>  ```
>  * buf - mesto u memoriji u koju se prima poruka
>  * count - broj podataka koji se prima
>  * dtype - tip podataka koji se prima
>  * source - rank procesa koji poruku šalje
>  * tag - oznaka poruke
>  * comm - komunikator u kom se obavlja operacija
>  * status - kada je source = MPI_ANY_SOURCE i tag = MPI_ANY_TAG onda status sadrži info o izvoru, oznaci i broju primljenih podataka
# Point-to-point funkcije bez blokiranja 

> MPI_Send bez blokiranja:
> ```c
> int MPI_Isend(void* buf, int count, MPI_Datatype dtype, int dest, int tag, MPI_Comm comm, MPI_Request* request)
> ```
> Dodat je **request** koji predstavlja id komunikacionog događaja i na osnovu njega se proverava status inicirane operacije ili se kompletira njeno izvršenje. 

> MPI Recv bez blokiranja:
> ```c
> int MPI_Irecv(void* buf, int count, MPI_Datatype dtype, int source, int tag, MPI_Comm comm, MPI_Request* request)
> ```

> Funkcija za proveru kompletiranja operacija bez blokiranja:
> ```c
> int MPI_Wait(MPI_Request* request, MPI_Status* status)
> ```
> ako je inicirana MPI_Irecv status čuva informaciju o izvoru poruke, oznaci poruke kao i count
> ako je inicirana MPI_Isend status čiva informacije o grešci

> Funkcija za proveru kompeltiranja operacija bez blokiranja:
> ```c
> int MPI_Test(MPI_Request* request, int falg, MPI_Status* status)
> ```
> flag je true ako je operacija koja je identifikovana sa request završena, u suprtonom je false

# Grupne operacije

Postoje 3 vrste 
