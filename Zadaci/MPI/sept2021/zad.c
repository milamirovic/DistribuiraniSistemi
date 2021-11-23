#define k
#define m
#define n
#define l  
void main(int argc, char **argv)
{
    int a[k][m], b[m][n], c[k][n], i, j, q, p;
    int rank, size;
    int master = 0;
    int local_a[l][m];
    struct { int val; int rank; } max, gmax;

    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    if(rank == master) 
    {
        //inicijalizacija
        for(i=0;i<k;i++)
        {
            for(j=0;j<m;j++)
            {
                a[i][j] = i+j;
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

    //master salje svakom procesu po l vrsta matrice A, gde je k deljivo sa l, 
    //i celu matricu B

    if(rank == master) 
    {
        MPI_Scatter(&a[0][0], l * m, MPI_INT, &local_a[0][0], 1, MPI_INT, master, MPI_COMM_WORLD);
        MPI_Bcast(&b[0][0], m * n, MPI_INT, master, MPI_COMM_WORLD);
    }

    //prozivod A[k][m] x B[m][n] = C[k][n]
    int localProd[l][n];//inicijalizovati elemenete na sve 0!
    max.val = INT_MIN;
    int localColProd[m];//inicijalizovati sve elemente na 1!
    int colProd[m];
    for(i=0;i<l;i++)
    {
        //za svaku kolonu (od njih l) matrice A u trenutnom prosu rank
        //svaku treba pomonoziti sa svakom od vrsta matrice b
        //b[m][n], mini matrica A u procesu je local_a[l][m]
        for(j=0;j<n;j++) 
        {
            localProd[i][j] = 0;
            for(q=0;q<m;q++) 
            {
                localProd[i][j] += local_a[i][q] * b[q][j];
            }
            //treba nam maximum matrice A i  rank njenog procesa
            if(localProd[i][j] > max.val)
            {
                max.val = localProd[i][j];
                max.rank = rank;
            }
        }

        //treba nam i prozivod elemenata matrice A po kolonama 
        //a mi elemente imamo po vrstama...

        for(i=0;i<m;i++)
        {
            localColProd[i] *= local_a[i][j];
        }
    }

    MPI_Reduce(&max, &gmax, 1, MPI_2INT, MPI_MAXLOC, master, MPI_COMM_WORLD);
    MPI_Bcast(&gmax, 1, MPI_2INT, master, MPI_COMM_WORLD);

    MPI_Gather(&localProd[0][0], l*n, MPI_INT, &c[0][0], 1, MPI_INT, gmax.rank, MPI_COMM_WORLD);

    MPI_Reduce(&localColProd[0], &colProd[0], m, MPI_INT, MPI_PROD, gmax.rank, MPI_COMM_WORLD);
    
    if(rank == gmax.rank)
    {
        printf("Prozivod A x B = C:");
        for(i=0;i<k;i++)
        {
            for(j=0;j<n;j++)
            {
                printf("c[%d][%d]=%d ", i, j, c[i][j]);
            }
            pritnf("\n");
        }

        pritnf("Maximum matrice A je %d.", gmax.value);

        printf("Proizvodi elemenata po kolonama matrice A su:");
        for(i=0;i<m;i++)
        {
            pritf("kolona %d = %d\n", i, colProd[i]);
        }
    }

}
