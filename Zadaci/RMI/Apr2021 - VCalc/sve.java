import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class VCalcServer
{
    public String rmiStr;
    public int port;

    public VCalcServer(String port, String host, String service)
    {
        this.rmiStr = "rmi://"+host+":"+port+"/"+service;
        this.port = Integer.parseInt(port);
    }
    public static void main(String[] args)
    {
        try
        {
            VCalcServer server = new VCalcServer("localhost", "1099", "VCalcService");
            LocateRegistry.createRegistry(server.port);
            System.out.println("java RMI registry created.");

            VCalcManager mngr = new VCalcManagerImpl();
            Naming.rebin(server.rmiStr, mngr);
        }
        catch(RemoteException e)
        {
            System.out.println("Error: " + e);
        }
    }
}

public class VCalcRequest implements Serializable
{
    private int cId;
    private Vector<Double> a;
    private Vector<Double> b;
    private VCalcCallback cb;

    public VCalcRequest(int c, Double a1, Double a2, Double b1, Double b2)
    {
        super();

        this.cId = c;
        this.a = new Vector<Double>();
        this.a.add(a1);
        this.a.add(a2);
        this.b = new Vector<Double>();
        this.b.add(b1);
        this.b.add(b2);
        this.cb = null;
    }

    public VCalcRequest(int c, Vector<Double> a, Vector<Double> b, VCalcCallback callback)
    {
        super();
        
        this.cId = c;
        this.a = new Vector<Double>(a);
        this.b = new Vector<Double>(b);
        this.cb = callback;
    }

    public int vratiCID()
    {
        return this.cId;
    }

    public Vector<Double> vratiA()
    {
        return this.a;
    }

    public Vector<Double> vratiB()
    {
        return this.b;
    }

    public VCalcCallback vratiCallback()
    {
        return this.cb;
    }

    public void setCallback(VCalcCallback cb)
    {
        this.cb = cb;
    }

    public Double operate()
    {
        if(this.a == null || this.b == null || this.a.size() != this.b.size())
        {
            System.out.println("Nije moguce izvrsiti operaciju!");
            return null;
        }
        
        Double rez = 0.0;
        for(int i=0;i<this.a.size();i++)
        {
            rez += this.a.get(i) * this.b.get(i);
        }

        return rez;
    }

    public void print()
    {
        System.out.println("CID: " + this.cId);

        System.out.println("a: [");
        for(Double d: this.a)
        {
            System.out.println(d);
        }
        System.out.println("]");

        System.out.println("a: [");
        for(Double d: this.a)
        {
            System.out.println(d);
        }
        System.out.println("]");
    }
}

public interface VCalcCallback extends java.rmi.Remote
{
    public void onDone(int cId, double result) throws RemoteException;
}

public interface CalcManager extends java.rmi.Remote
{
    public int SendVCalcRequest(VCalcRequest req) throws RemoteException;
    //preuzima zahtev za izracunavanjem i stavlja ga u red cekanja, pri tome 
    //generise se i vraca jedinstveni identifikator trenutnog izracunavanja

    public bool RunNextVCalc() throws RemoteException;
    //preuzima prvo zahtev iz reda cekanja, izvrsava zadatu operaciju nad vektorima
    //i poziva odgovarajuci callback metodu i vraca true
}

public class CalcManagerImpl extends UnicastRemoteObject implements CalcManager
{
    private LinkedList<VCalcRequest> requests;//ovo je RED CEKANJA
    private int head;

    public CalcManagerImpl() throws RemoteException
    {
        super();
        this.requests = new LinkedList<VCalcRequest>();
        this.head = 1;
    }

    public int SendVCalcRequest(VCalcRequest request) throws RemoteException
    {
        //preuzima zahtev za izracunavanjem i stavlja ga u red cekanja, pri tome 
        //generise se i vraca jedinstveni identifikator trenutnog izracunavanja

        if(request == null || request.vratiCallback() == null || request.vratiA() == null || request.vratiB() == null || request.c == null || request.a.size() != request.b.size()) 
        {
            return -1;
        }
        //od datog requesta kreiramo novi koji ima id kao redni broj u redu requests 
        VCalcRequest req = new VCalcRequest(this.head, request.vratiA(), request.vratiB(), request.vratiCallback());
        //stavljamo ovaj request u red requesta
        this.requests.addLast(req);
        //povecavamo redni broj
        this.head++;
        return this.head;
    }

    public bool RunNextVCalc() throws RemoteException
    {
        //preuzima prvi zahtev iz reda cekanja, izvrsava zadatu operaciju nad vektorima
        //i poziva odgovarajuci callback metodu i vraca true

        if(this.requests.size() == 0) {
            return false;
        }
        //preuzima prvi zahtev iz reda cekanja
        VCalcRequest tmp = this.requests.pollFirst();//kao pop, izbaci i vrati prvog
        //izvrsava zadatu operaciju nad vektorima koji se nalaze u tmp VCalcRequest-u
        Double rezultat = tmp.operate();

        if(rezultat == null) {
            return false;
        }
        //poziva odgovarajucu callback metodu onDone
        tmp.vratiCallback().onDone(tmp.vratiCID(), rezultat);
        //vraca true
        return true;
    }
}

public class VCalcClient 
{
    private VCalcManager mngr = null;
    private VCalcCallback cb = null;
    private VCalcRequest request; 

    public VCalcClient()
    {

    }

    public class VCalcCallbackImpl extends UnicastRemoteObject implements VCalcCallback
    {
        public VCalcCallbackImpl() throws RemoteException
        {
            super();
        }

        public void onDone(ind cid, Double result) throws RemoteException
        {
            show(cid, result);
        }
    }

    public void show(int cid, Double result) 
    {
        System.out.println("CID: " + cid);
        System.out.println("Rezultat izracunavanja je: " + result);
    }

    public void run() 
    {
        try
        {
            this.mngr = (VCalcManager)Naming.Lookup("rmi://localhost:1099/VCalcService");
            this.cb = new VCalcCallbackImpl();
            mngr.register(cb);
        }
        catch(RemoteException e)
        {

        }

        try
        {
            Scanner scn = new Scanner(System.in);

            System.out.println("Pocetak...");
            for (int i = 0; i < 2; i++)
                client.sendVCalcRequest(new VCalcRequest(0, new Vector<Double>() {
                    {
                        add(Math.random() * 100);
                        add(Math.random() * -20);
                        add(Math.random() * 55);
                    }
                }, new Vector<Double>() {
                    {
                        add(Math.random() * -11);
                        add(Math.random() * 60);
                        add(Math.random() * 100);
                    }
                }, client));

            client.runNextVCalc();

            System.out.println("Pritisnite enter za kraj...");
            scn.nextLine();
            scn.close();
        }
        catch(Exception e) { }
    }
}
