//NIJE TACAN, ISPRAVICU NEKADA
#include <mpi.h>
#include <stdio.h>
#define k 10
#define m 8
#define l 2

void main(int argc, char **argv)
{
    //       A           b
    //   |* * * *|
    //   |* * * *| x |* * * *|
    // k |* * * *|
    //   |* * * *|
    //   |* * * *|
    //   ____m____   ____m____

    int a[k][m], b[m], c[k], rank, size, koloneIn[l][k], kolonaOut[l][k], master=0;
    int vrednost[l], i, j, sum=0, vrsta[m], sume[k];
    struct { int value; int rank; } max, gmax;
    max.value = INT_MIN;

    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD,&rank);
    MPI_Comm_size(MPI_COMM_WORLD,&size);

    //max vrednost elementa u matrici A
    //sumu elemenata svake vrste matrice A
    //master proces salje svakom procesu l kolona matrice A (l je zadata konstanta, l|m)
    //i po l elemenata vektora b
    //sve se prikazuje u procesu u kome je max vrednost elemenata u matrici A

    if(rank == master)
    {
        //inicijalizacija a i b
        for(i=0;i<k;i++)
        {
            for(j=0;j<m;j++)
            {
                a[i][j] = j + i;
            }
        }

        for(i=0;i<m;i++)
        {
            b[i] = i + rank;
        }
    }

    if(rank == master)
    {
        //za P0 kolona prvih l kolona 
        for(i=0;i<l;i++)
        {
            for(j=0;j<k;j++)
            {
                koloneOut[j][i] = a[j][i];
            }
        }

        for(proces=1;proces<size;proces++)
        {
            //proces Ph dobija sledecih h kolona matrice A
            for(i=0;i<l;i++)
            {
                for(j=0;j<k;j++)
                {
                    koloneIn[j][i] = a[j][proces*l + i];
                }
            }
            MPI_Send(&koloneIn[0][0], l*k, MPI_INT, proces, 0, MPI_COMM_WORLD);
        }
    }
    else
    {
        MPI_Recv(&koloneOut, l*k, MPI_INT, 0, 0, MPI_COMM_WORLD);
    }
    //poslati su elementi iz A

    MPI_Scatter(&b[0], l, MPI_INT, &Lelemenata, l, MPI_INT, 0, MPI_COMM_WORLD);
    //poslati su i elementi iz b

    //obaviti mnozenje:
    for(i=0;i<l;i++)
    {
        vrednost[i] = 0;
        //za svaku kolonu i element obaviti sledece mnozenje:
        for(j=0;j<k;j++)
        {
            vrednost[i] += koloneOut[j][i] * Lelemenata[i];
        }
    }

    //sad prvo nalazimo max u matrici a i njegov proces, da bismo njemu slali rezultat mnozenja a i b
    //jer, sve se prikazuje u procesu u kome je max vrednost elemenata u matrici A

    for(i=0;i<l;i++)
    {
        for(j=0;j<k;j++)
        {
            if(max.value<koloneOut[i][j])
            {
                max.value = kolonaOut[i][j];
                max.rank = rank;
            }
        }
    }

    //nadjemo max, pa globalni max i kazemo svim procesima koji je to gmax:
    MPI_Reduce(&max, &gmax, 1, MPI_2INT, MPI_MAXLOC, master, MPI_COMM_WORLD);
    MPI_BCast(&gmax, 1, MPI_2INT, master, MPI_COMM_WORLD);
    //max vrednost elementa u matrici A - gmax.value - vidi i gmax.rank jer je uradjen Bcast!

    //sada mozemo da posaljemo izmnozene mat i vekt na pravi proces max.rank
    //skupiti sve vrednosti sa Gather u jedan vektor c:
    for(i=0;i<l;i++)
    {
        MPI_Gather(&vrednost[i], 1, MPI_INT, &c, 1, MPI_INT, gmax.rank, MPI_COMM_WORLD);
    }
    //rezultat mnozenja A i b je u c u procesu gmax.rank
    
    MPI_Scatter(&a[0][0], m, MPI_INT, &vrsta, m, MPI_INT, master, MPI_COMM_WORLD);
    //podelimo matricu po vrstama po procesima

    //kako bez scatter da izvucemo vrste?
    // for(v=0;v<k;v++)
    // {
    //     for(i=0;i<l;i++)
    //     {
    //         MPI_Gather(koloneOut[i][v], 1, MPI_INT, &vrsta[v], 1, MPI_INT, master, MPI_COMM_WORLD);
    //     }
    // }

    //sada nadjimo sumu elemenata svake vrste matrice A:
    for(i=0;i<m;i++)
    {
        sum += vrsta[i];
    }

    MPI_Gather(&sum, 1, MPI_INT, &sume, 1, MPI_INT, gmax.rank, MPI_COMM_WORLD);
    //sad imamo sve

    if(rank == gmax.rank)
    {
        printf("Rezultat mnozenja matrice A i vektora b:");
        for(i=0;i<k;i++)
        {
            printf("c[%d] = %d ", i, c[i]);
        }

        pritnf("Maximalni element matrice A je %d i on je u procesu %d", gmax.value, gmax.rank);

        printf("Zbirovi elemenata po vrstama matrice A:");
        for(i=0;i<m;i++)
        {
            printf("zbir po vrtsi %d je %d \n", i, sume[i]);
        }
    }

    MPI_Finalize();
}
