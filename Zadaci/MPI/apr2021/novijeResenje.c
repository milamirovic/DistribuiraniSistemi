#include "mpi.h"
#define n 3
#define k 2
#define master 5

/*
    Anxk      x     bk      =       Cn
    x x             x             x x x  
    x x             x
    x x                         
*/

void main(int argc, char** argv)
{
    int rank, size, A[n][k], b[k], c[n], local_c, kolAIn[n], kolA[n], elementB, prod_local, prod[n];
    prod_local = 1;
    local_c = 0;

    struct {int val; int rank;} min, gmin;
    MPI_Status status;

    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    if(rank == master) 
    {
        //inicijalizacija
        for(int i=0;i<n;i++)
        {
            for(int j=0;j<k;j++)
            {
                a[i][j] = i+j;
            }
        }
        for(int i=0;i<k;i++)
        {
            b[i] = i+1;
        }

        //master salje po kolonu matrice A
        //prvo za proces master
        for(int i=0;i<n;i++)
        {
            kolA[i] = A[i][master];
        }

        for(int j=0;j<k;j++)
        {
            if(j!=master)
            {
                for(int i=0;i<n;i++)
                {
                    kolAIn[i] = a[i][j];
                }
                MPI_Send(&kolAIn[0], n, MPI_INT, j, 0, MPI_COMM_WORLD);
            }
        }
    }
    else
    {
        //ako nije master onda prima kolonu od mastera
        MPI_Recv(&kolA[0], n, MPI_INT, master, 0, MPI_COMM_WORLD, &status);
    }

    //sad se salje element po element niza B
    MPI_Scatter(&b[0], 1, MPI_INT, &elementB, 1, MPI_INT, master, MPI_COMM_WORLD);

    //sad mozemo da mnozimo kolonu sa 1 elementom i dobijamo element 1 rezultujuceg vektora
    for(int i=0;i<n;i++)
    {
        local_c += kolA[i] * elementB;
    }
    //sad treba sve te elemente da spojimo u rezultujuci vektor c, ali to se radi u procesu
    //ciji je rank jednak gmin.rank, a mi jos nismo nasli to, pa prvo nadjimo to

    //sad racunamo MIN
    min.val = INT_MAX;
    for(int i=0;i<n;i++)
    {
        if(min.val > local_c[i]) 
        {
            min.val = local_c[i];
            min.rank = rank;
        }
    }
    //ovo je min po koloni, sad nam treba jedan globalni min
    MPI_Reduce(&min, &gmin, 1, MPI_2INT, MPI_MINLOC, MPI_COMM_WORLD, master);

    //sad ga saljemo svima da znaju ko je gmin  
    MPI_Bcast(&gmin, 1, MPI_2INT, master, MPI_COMM_WORLD);

    //sad imamo rank procesa koji sadrzi min element matrice A i mozemo u njemu da smestimo 
    //rezultat mnozenja matrice A i vektora b:
    MPI_Gather(&local_c, 1, MPI_INT, &c, 1, MPI_INT, gmin.rank, MPI_COMM_WORLD);

    //sad moze proizvod elemenata svake vrste matrice Anxk
    for(int i=0;i<k;i++)
    {
        prod_local *= a[rank][i];
    }

    //imamo proizvod p ojednoj vrsti, sad moramo da ih spojimo sve u jedan niz od n elemenata
    MPI_Gather(&prod_local, 1, MPI_INT, &prod, 1, MPI_INT, gmin.rank, MPI_COMM_WORLD);

    //sad imamo sve, mozemo da stampamo
    if(rank == gmin.rank) 
    {
        printf("A x b = C: [");
        for(int i=0;i<n;i++)
        {
            printf("%d ", c[i]);
        }
        printf("]\n");

        printf("Minn element matrice A je %d.\n", gmin.val);

        printf("Proizvodi elemenata po vrstama matrice A: \n");
        for(int i=0;i<n;i++)
        {
            printf("Proizvod po vrsti %d je %d\n", i, prod[i]);
        }
    }

    MPI_Finalize();
}
