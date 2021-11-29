```java
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;

public class Server
{
    private BingoManager mngr;

    public BingoServer(String host, String port, String service)
    {
        LocateRegistry.createRegistry(Integer.ParseInt(port));
        mngr = new BingoManager();
        Naming.rebind("rmi://" + host + ":" + port + "/" + service, mngr);
    }

    public static void main(String[] args)
    {
        try
        {
            BingoServer server = new BingoServer("localhost", "1099", "BingoService");

            Scanner scn = new Scanner(System.in);
            scn.nextLine();
            scn.close();

            System.exit(0);
        }
        catch(RemoteException e)
        {

        }
    }
}

public class Ticket implements Serializable
{
    private int id; //jedinstven id
    private Vector<Integer> numbers; //niz brojeva koje igra korisnik
    private BingoCallback callback;

    public Ticket(int id, Vector<Integer> numbers, BingoCallback callback)
    {
        this.id = id;
        this.numbers = numbers;
        this.callback = callback;
    }

    public void AddNumber(Integer number)
    {
        this.numbers.add(number);
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setNumbers(Vector<Integer> numbers)
    {
        this.numbers = numbers;
    }

    public void setCallback(BingoCallback callback)
    {
        this.callback = callback;
    }

    public int getId()
    {
        return this.id;
    }

    public Vector<Integer> getNumbers()
    {
        return this.numbers;
    }

    public BingoCallback getCallback()
    {
        return this.callback;
    }
}

public interface BingoManager extends Remote
{
    public Ticket playTicket(Vector<Integer> numbers, BingoCallback cb) throws RemoteException;

    public void drawNumber() throws RemoteException;
}

public class BingoManagerImpl extends UnicastRemoteObject implements BingoManager
{
    private LinkedList<Ticket> tickets;
    private boolean obavljenoJeIzvlacenje = false;
    private ArrayList<Integer> izvuceniBrojevi;

    public BingoManagerImpl() 
    {
        tickets = new LinkedList<Ticket>();
        izvuceniBrojevi = new ArrayList<Integer>();
    }

    public Ticket playTicket(Vector<Integer> numbers, BingoCallback cb) throws RemoteException
    {
        //registruje ticket u sistemu i dodeliti mu jedinstven id 
        //ako se pozove fja nakon sto je zavrseno izvlacenje potrebno je vratiti null
        if(obavljenoJeIzvlacenje)
        {
            return null;
        }

        Ticket novi = new Ticket(this.tickets.size(), numbers, cb);
        this.tickets.add(novi);
        return novi;
    }

    public void drawNumber() throws RemoteException
    {
        //ivlaci jedan od 90 mogucih brojeva i nakon svakog izvucenog broja 
        //proverava da li neki od igraca ima tiket sa izvucenim brojevima do sada i u tom
        //slucaju zove njegovu callback metodu isWWinner() koje je definisana u okviru BingoCallback 
        //dok kod ostalih poziva isNotWinner()

        int broj = (Integer) (Math.random() * 90 + 1);
        while(this.izvucenBrojevi.contains(broj))
        {
            broj = (Integer) (Math.random() * 90 + 1);
        }
        this.izvucenBrojevi.Add(broj);

        this.obavljenoJeIzvlacenje = true;

        //sad trazimo igrace koji imaju tiket koji ima sve do sada izvucene brojeve
        for(Ticket t: tickets)
        {
            int count = 0;
            for(Integer i : this.izvucenBrojevi)
            {
                if(t.getNumbers().contains(i))
                {
                    count++;
                }
            }
            if(count == tickets.size() && tickets.size()!=15)
            {
                //znaci taj ticket ima sve do sada brojeve :)
                //nismo stigli do kraja igre jos
                t.getCallback().isWinner("Vi ste potencijalni pobednik!");
            }
            else if(count == tickets.size() && tickets.size() == 15)
            {
                t.getCallback().isWinner("Vi ste pobednik!");
            }
            else
            {
                t.getCallback().isNotWinner("Vi niste pobednik!");
            }
        }
    }
}

public interface BingoCallback extends Remote
{
    public void isWinner(String poruka) throws RemoteException;
    public void isNotWinner(String poruka) throws RemoteException;
}

public class BingoClient 
{
    private BingoManager manager;
    private BingoCallback callback;

    public BingoClient(String host, String port, String service)
    {
        manager = (BingoManager) Naming.Lookup("rmi:/" + host + ":" + port + "/" + service);
        callback = new BingoCallbackImpl();
    }

    public BingoManager getManager()
    {
        return this.manager;
    }

    public BingoCallback getCallback()
    {
        return this.callback;
    }

    public class BingoCallbackImpl extends UnicastRemoteObject implements BingoCallback
    {
        public BingoCallbackImpl() throws RemoteException
        {
            super();
        }

        public void isWinner(String poruka) throws RemoteException
        {
            System.out.println("Vi ste moguci pobednik!");
        }

        public void isNotWinner(String poruka) throws RemoteException
        {
            System.out.println("Vi niste moguci pobednik!");
        }
    }

    public static void main(String[] args) 
    {
        try
        {
            BingoClient client = new BingoClient();
            Scanner s = new Scanner(System.in);

            System.out.println("****************BINGO*****************");
            System.out.println("Unesite 15 brojeva od 0 do 90: \n");
            ArrayList<Integer> list = new ArrayList<Integer>();
            for(int i=0;i<15;i++) 
            {
                Integer k = Integer.ParseInt(s.nextLine().trim());
                while(list.contains(k)) 
                {
                    System.out.println("Taj broj ste vec uneli, pokusajte ponovo...\n");
                    k = Integer.ParseInt(s.nextLine().trim());
                }
                list[i]=k;
            }
            //sad registrujemo ticket sa funkcijom playTicket
            client.getManager().playTicket(list, client.getCallback());

            System.out.println("Krece ivlacenje brojeva!");

            for(int i=0;i<15;i++)
            {
                client.getManager().drawNumber();
            }

            System.out.println("Igra je zavrsena!");
            s.nextLine();
            s.close();

            System.exit(0);
        }
        catch(RemoteException e)
        {

        }
    }
}
```
