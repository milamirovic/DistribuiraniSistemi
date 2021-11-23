# Tekst zadatka:
Napisati MPI program koji izračunava i prikazuje proizvod matrice **Anxk** i vektora **bkx** i generiše vektor c. Matrica A i vektor b se inicijalizuju u master procesu. Izračunavanje se obavlja tako što master proces šalje svakom procesu po 1 kolonu matrice A i po jedan element vektora b. Svi elementi jedne kolone matrice A se šalju odjednom. Svi procesi učestvuju u izračunavanjima potrebnim za generisanje rezultata programa. Nakon slanja elemenata matrice A, u okviru programa se izračunava i prikazuje minimum vrednosti elemenata matrice A, kao i proizvod elemenata svake vrste matrice A. Program treba da obezbedi da se rezultati programa nalaze i prikazuju u procesu koji nakon slanja elemenata matrice A sadi minimum svih vrednosti u matrici A. Zadatak rešiti:
* korišćenjem grupnih operacija, osim za slanje kolona matrice A
* korišćenjem Point-to-point operacija
```c
#define n
#define k

void main(int argc, char **argv)
{
    int a[n][k], b[k], c[n], rank, size;
    int master = 0;
    MPI_Status status;
    struct { int value; int rank; } min, gmin;
    min.value = INT_MAX;

    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    if(rank == master) 
    {
        //inicijalizacija a i b
        for(i=0;i<n;i++)
        {
            for(j=0;j<k;j++)
            {
                a[i][j] = i + j;
            }
        }

        for(i=0;i<k;i++)
        {
            b[i] = i + rank;
        }
    }

    if(rank == master) 
    {
        //master svima salje po 1 kolonu matrice A i po 1 element niza B
        //kolone iz A se salji sa point to point

        //za proces P0 ide kolona 0
        for(i=0;i<n;i++)
        {
            kolona[i] = a[i][master];
        }

        for(proces=master;proces<size;proces++)
        {
            for(i=0;i<n;i++)
            {
                kolona[i] = a[i][proces];
            }
            MPI_Send(&kolona, n, MPI_INT, proces, MPI_COMM_WORLD);
        }
        //ako master nije 0 onda mora i for petlja za sve do master procesa
    }
    else 
    {
        MPI_Recv(&kolonaOut, 1, MPI_INT, master, 0, MPI_COMM_WORLD, &status);
    }

    //poslate kolone matrice A, sad elementi niza B

    MPI_Scatter(&b[0], 1, MPI_INT, &element, 1, MPI_INT, master, MPI_COMM_WORLD);
    //poslati i elementi niza B 

    //naci min u matrici A
    for(i=0;i<n;i++)
    {
        if(kolona[i]<min.value)
        {
            min.value = kolona[i];
            min.rank = rank;
        }
    }

    //nasli smo min, sad uradimo reduce da nadjemo globalni min - gmin 
    MPI_Reduce(&min, &gmin, MPI_2INT, MPI_MINLOC, master, MPI_COMM_WORLD);
    //posaljimo gmin svim procesima
    MPI_Bcast(&gmin, 1, MPI_2INT, master, MPI_COMM_WORLD);

    //sad imamo rank koji ce da pamti resenje i rezultate
    //mozemo da nadjemo proizvod A[n][k] x B [k] = C[n]
    int localProd[n], prod[n];
    for(i=0;i<n;i++)
    {
        localProd[i] = kolona[i] * element;
    }

    MPI_Reduce(&localProd, &c, n, MPI_INT, MPI_SUM, gmin.rank, MPI_COMM_WORLD);
    //imamo konacni proizvod i to u procesu gmin.rank 

    //sad treba naci proizvod elemenata matrice A po vrsti
    int rowProd = 1;
    int rowProds[n];
    for(i=0;i<k;i++)
    {
        //vrsta[i] = a[rank][i];
        rowProd *= a[rank][i];
    }
    MPI_Gather(&rowProd, 1, MPI_INT, &rowProds[0], 1, MPI_INT, gmin.rank, MPI_COMM_WORLD);

    if(rank == gmin.rank) 
    {
        printf("A[n][k] x B[k] = C[n]: ");
        for(i=0;i<n;i++)
        {
            printf("c[%d]=%d ", i, c[i]);
        }

        printf("Minimum vrednost u matrici A je %d.", gmin.value);

        printf("Proizvodi elemenata matrice A po vrstama su:");
        for(i=0;i<n;i++)
        {
            printf("vrsta %d = %d \n", i, rowProds[i]);
        }
    }

    MPI_Finalize();
}
```
