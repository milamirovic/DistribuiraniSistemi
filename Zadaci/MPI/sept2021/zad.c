#define k 10
#define m 7 
#define n 6
#define l 2

void main(int argc, char** argv)
{
    int a[k][m], b[m][n], c[k][n], rank, size, i, j, q, p;
    int master = 0;
    struct { int value; int rank; } max, gmax;
    int parcijalColProd[m], colProd[m];
    
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    if(rank == master) 
    {
        //inicijalizacija a i b
        for(i=0;i<k;i++)
        {
            for(j=0;j<m;j++)
            {
                a[i][j] = i + j;
            }
        }

        for(i=0;i<m;i++)
        {
            for(j=0;j<n;j++)
            {
                b[i][j] = rank * i + j;
            }
        }
    }

    //proizvod A x B = C
    //master salje svima po l vrsta (l deli k) i celu matricu B
    //rezultat je u procesu koji ima max svih vrednosti u matrici C

    if(rank == master)
    {
        MPI_Scatter(&a[0][0], l * m, MPI_INT, &Lvrsta[0][0], l * m, MPI_INT, master, MPI_COMM_WORLD);
        //dobijamo mini matrice l x m
        MPI_Bcast(&b[0][0], m * n, MPI_INT, master, MPI_COMM_WORLD);
    }

    max.value = INT_MIN;

    for(i=0;i<l;i++)
    {
        //mnozimo matricu l x m i matricu b (m x n) i dobijamo matricu l x n
        //za svaku vrstu od l 
        for(j=0;j<n;j++)
        {
            parcijalC[i][j] = 0;
            for(h=0;h<m;h++)
            {
                parcijalC[i][j] += Lvrsta[i][h] * b[h][j];
            }

            if(pacijalc[i][j] > max.value)
            {
                max.value = pacijalc[i][j];
                max.rank = rank;
            }
        }
        for(j=0;j<m;j++)
        {
            parcijalColProd[j] *= Lvrsta[i][j];
        }
    }

    MPI_Reduce(&max, &gmax, 1, MPI_2INT, MPI_MAXLOC, master, MPI_COMM_WORLD);
    MPI_Bcast(&gmax, 1, MPI_2INT, master, MPI_COMM_WORLD);

    MPI_Gather(&parcijalC[0][0], l * n, MPI_INT, &c[0][0], l * n, gmax.rank, MPI_COMM_WORLD);
    MPI_Reduce(&parcijalColProd[0], &colProd[0], m, MPI_INT, MPI_PROD, gmax.rank, MPI_COMM_WORLD);

    if(rank == gmax.rank)
    {
        printf("Proizvod A x B = C:");
        for(i=0;i<k;i++)
        {
            for(j=0;j<n;j++)
            {
                printf("c[%d][%d] = %d ", i, j, c[i][j]);
            }
            printf("\n");
        }

        printf("Proizvod elemenata matrice A po kolonama:");
        for(i=0;i<m;i++)
        {
            printf("prozvod po koloni %d je %d\n", i, colProd[i]);
        }

        printf("Max vrednost matrice A je %d sa rankom %d.", gmax.value, gmax.rank);
    }

    MPI_Finalize();
}
