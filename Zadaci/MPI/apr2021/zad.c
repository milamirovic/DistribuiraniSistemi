#include <mpi.h>
#include <stdio.h>
#define n 5
#define k 6

void main(int argc, char **argv)
{
    //n x k = 3 x 4
    //   |* * * *|     |*|
    // n |* * * *|  x  |*|  k
    //   |* * * *|     |*|
    //   ____k____     |*|
    //                  1

    int a[n][k], b[k], c[k], i, j, kolonaIn[n], kolonaOut[n], proces, element, vrsta[k];
    int rank, size, master = 0, proizvodVrste=1, prozivodiVrsta[n];
    int proizvodParcijal, proizvodFinal[k];
    struct { int value; int rank; } min;

    MPI_Init(&arc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    //inicijalizacija a i b u master procesu
    if(rank == master) 
    {
        for(i=0;i<n;i++)
        {
            for(j=0;j<k;j++)
            {
                a[i][j] = i+j;
            }
        }

        for(i=0;i<k;i++)
        {
            b[i] = i + rank;
        }
    }

    //po kolonu matrice a i element vektora b
    if(rank == master)
    {
        //za proces P0 treba nam kolona 0
        for(i=0;i<n;i++)
        {
            kolonaOut[i] = a[i][0];
        }
        for(proces=1;proces<size;proces++)
        {
            //za P1 kolona 1
            //za P2 kolona 2
            // ...
            for(i=0;i<n;i++)
            {
                kolonaIn[i] = a[i][proces];
            }

            MPI_Send(kolonaIn, n, MPI_INT, proces, 0, MPI_COMM_WORLD);
        }
    }
    else
    {
        MPI_Recv(kolonaOut, n, MPI_INT, 0, 0, MPI_COMM_WORLD);
    }

    //poslate su kolone matrice, sad treba elementi vektora b

    MPI_Scatter(&b[0], 1, MPI_INT, &element, 1, MPI_INT, 0, MPI_COMM_WORLD);

    //poslati su i elementi vektora b

    for(i=0;i<n;i++)
    {
        proizvodParcijal += kolonaOut[i] * element;
    }
    //izracunat je prozivod za tu kolonu i taj element

    MPI_Gather(&proizvodParcijal, 1, MPI_INT, &parcijalFinal, 1, MPI_INT, 0, MPI_COMM_WORLD);
    //spojeni svi proizvodParcijal u niz proizvodFinal

    //izracunavanje i prikaz minimuma vrednosti matrice A
    //radi se i proizvod elemenata svake vrste matrice A
    //resenja prikazati u procesu koji sadrzi minimum matrice A

    min.value = INT_MAX;

    for(i=0;i<n;i++)
    {
        if(kolonaOut[i]<min.value)
        {
            min.value = kolonaOut[i];
            min.rank = rank;
        }
    }

    MPI_Scatter(&a[0][0], k, MPI_INT, &vrsta, k, MPI_INT, 0, MPI_COMM_WORLD);
    //posaljemo vrste po procesima 

    for(i=0;i<k;i++)
    {
        proizvodVrste*=vrsta[i];
    }
   // izracunamo proizvod za vrstu

   MPI_Gather(&prozivodVrste, 1, MPI_INT, &proizvodiVrsta, 1, MPI_INT, 0, MPI_COMM_WORLD);
   //skupimo proizvode vrsta u jedan niz

   if(rank == min.rank) 
   {
       printf("Proizvod matrica A i vektora b: ");
       for(i=0;i<k;i++)
       {
           printf("c[ %d ] = %d ", i, proizvodFinal[i]);
       }

       printf("Minimum matrice A je %d, a rank je tada %d ", min.value, min.rank);

       printf("Proizvodi elemenata po vrstama: ");
       for(i=0;i<n;i++)
       {
           printf("proizvod po vrsti %d je %d \n", i, proizvodFinal[i]);
       }
   }

   MPI_Finalize();
}
