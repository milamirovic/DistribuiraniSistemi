import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

/* LOTO IGRE NA SRECU 7 od 39 brojeva */

public class LotoServer
{
    private BingoManager mngr;

    public LotoServer(String host, String port, String service)
    {
        LocateRegistry.createRegistry(Integer.parseInt(port));
        System.out.println("java rmi registry created.");
        this.mngr = new BingoManager();
        Naming.rebind("rmi://" + host + ":" +port+"/"+service, mngr);

    }

    public static void main(String[] args) 
    {
        try
        {
            LotoServer server = new LotoServer("localhost", "1099", "LotoService");
            Scanner s = new Scanner(System.in);

            System.out.println("Server ready.");

            s.nextLine();
            s.close();
            System.exit(0);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

public class Ticket implements Serializable
{
    private int id;
    private Vector<Integer> numbers;
    private LotoCallback callback;

    public Ticket(int id, Vector<Integer> numbers, LotoCallback callback)
    {
        this.id = id;
        this.numbers = numbers;
        this.callback = callback;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return this.id;
    }

    public void setNumbers(Vector<Integer> numbers)
    {
        this.numbers = numbers;
    }

    public Vector<Integer> getNumbers()
    {
        return this.numbers;
    }

    public void setCallback(LotoCallback callback)
    {
        this.callback = callback;
    }

    public LotoCallback getCallback()
    {
        return this.callback;
    }
}

public interface LotoManager extends Remote
{
    public Ticket playTicket(Vector<Integer> numbers) throws RemoteException;
    public Vector<Integer> getWinnersIds() throws RemoteException;
    public void drawNumbers() throws RemoteException;
}

public class LotoManagerImpl extends UnicastRemoteObject implements LotoManager
{
    private ArrayList<Ticket> tickets;
    private ArrayList<Integer> izvuceniBrojevi;
    private int ptr;
    private boolean obavljenoJeIzvlacenje;

    public LotoManagerImpl() throws RemoteException
    {
        this.tickets = new ArrayList<Ticket>();
        this.izvuceniBrojevi = new ArrayList<Integer>();
        this.ptr = 1;
        this.obavljenoJeIzvlacenje = false;
    }

    public Ticket playTicket(Vector<Integer> numbers, LotoCallback callback) throws RemoteException
    {
        //registruje tiket u sistemu i dodeli mu jedinstven id. ako se ova fja pozove
        //nakon sto je obavljeno izvlacenje vrati se null
        if(obavljenoJeIzvlacenje)
        {
            return null;
        }
        Ticket novi = new Ticket(this.ptr, numbers, callback);
        this.tickets.addLast(novi);
        this.ptr++;
        return novi;
    }

    public Vector<Integer> getWinnersIds() throws RemoteException
    {
        //vraca niz id-jeva za tikete koji imaju najvise pogodjenih brojeva
        ArrayList<Integer> winnersIds = new ArrayList<Ticket>();
        if(obavljenoJeIzvlacenje && this.izvuceniBrojevi.size() == 7) 
        {
            for(Ticket t: this.tickets)
            {
                int br = 0;
                for(Integer i: this.izvuceniBrojevi)
                {
                    if(t.getNumbers().equals(i))
                    {
                        br++;
                    }
                }
                if(br==7)
                {
                    winnersIds.add(t.getId());
                }
            }
        }
        return winnersIds;
    }

    public void drawNumbers() throws RemoteException
    {
        //izvuce 7 od 39 mogucih brojeva 
        for(int i=0;i<7;i++)
        {
            int num = (Integer)(Math.random() * 40) + 1;
            while(izvuceniBrojevi.contains(num))
            {
                //vec je izvucen taj broj, mora opet
                num = (Integer)(Math.random() *40) +1;
            }
            this.izvuceniBrojevi.add(num);
        }
        //brojevi su izvuceni
        this.obavljenoJeIzvlacenje = true;
        //odredimo sada ko su pobednici
        ArrayList<Integer> pobedniciIds = getWinnerIds();
        if(pobedniciIds.size()>0) 
        {
            for(Ticket t: this.tickets)
            {
                if(pobedniciIds.contains(t.getId()))
                {
                    //znaci ovaj t je pobednik
                    t.getCallback().notifyWinner();
                }
                else
                {
                    //ovaj t nije pobednik
                    t.getCallback().notifyLoser();
                }
            }
        }
    }
}

public interface LotoCallback extends Remote
{
    public void notifyWinner() throws RemoteException;
    public void notifyLoser() throws RemoteException;
}

public class LotoClient 
{
    private LotoManager manager;
    private LotoCallback callback;
    
    public LotoClient(String host, String port, String service)
    {
        this.manager = (LotoManager) Naming.Lookup("rmi://" + host + ":" + port + "/" + service);
        this.callback = new LotoCallbackImpl();
    }

    public LotoManager getManager()
    {
        return this.manager;
    }

    public LotoCallback getCallback()
    {
        return this.callback;
    }

    public class LotoCallbackImpl extends UnicastRemoteObject implements LotoCallback
    {
        public LotoCallbackImpl() throws RemoteException
        {
            super();
        }

        public void notifyWinner() throws RemoteException
        {
            System.out.println("Vi ste pobednik! Cestitamo!");
        }

        public void notifyLoser() throws RemoteException
        {
            System.out.println("Vi ste nazalost izgubili. Vise srece drugi put!");
        }
    }

    public static void main(String[] args) 
    {
        try
        {
            LotoClient client = new LotoClient("localhost", "1099", "LotoService");
            Scanner s = new Scanner(System.in);

            System.out.println("*****LOTO IGRA NA SRECU*******");
            System.out.println("Unesite 7 razlicitih brojeva od 1 do 39:");
            ArrayList<Integer> brojevi = new ArrayList<Integer>();
            for(int i=0;i<7;i++)
            {
                int num = Integer.parseInt(s.nextLine().trim());
                while(brojevi.contains(num))
                {
                    System.out.println("Broj " + num + " ste vec uneli. Pokusajte ponovo...");
                    num = Integer.parseInt(s.nextLine().trim());
                }
            }
            //uneti su svi brojevi, hajde da registrujemo tiket
            client.getManager().playTicket(brojevi, client.getCallback());

            System.out.println("Krece izvlacenje brojeva...");
            client.getManager().drawNumbers();

            System.out.println("Igra je zavrsena...");
            s.nextLine();
            s.close();

            System.exit(0);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}