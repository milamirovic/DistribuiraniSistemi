import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

public class VCalcServer 
{
    public String rmiString;
    public int port;

    public VCalcServer(String host, String port, String service)
    {
        this.rmiString = "rmi://" + host + ":" + port + "/" + service;
        this.port = Integer.parseInt(port);
    }

    public static void main(String[] args)
    {
        try
        {
            VCalcServer server = new VCalcServer("localhost", "1099", "VCalcService");
            VCalcManager mngr = new VCalcManagerImpl();
            Naming.rebind(server.rmiString, mngr);
            System.out.println("java rmi registry created.");
        }
        catch(Exception e) { }
    }
}

public class VCalcRequest implements Serializable 
{
    private int cid;
    private Vector<Double> a;
    private Vector<Double> b;
    private VCalcCallback cb;

    public VCalcRequest(int id, Vector<Double> a, Vector<Double> b, VCalcCallback cb)
    {
        this.cid = id;
        this.a = a;
        this.b = b;
        this.cb = cb;
    }

    public void setCid(int cid)
    {
        this.cid = cid;
    }

    public void setA(Vector<Double> a)
    {
        this.a = a;
    }

    public void setB(Vector<Double> b)
    {
        this.b = b;
    }

    public void setCallback(VCalcCallback callback)
    {
        this.cb = callback;
    }

    public int getCid()
    {
        return this.cid;
    }

    public Vector<Double> getA()
    {
        return this.a;
    }

    public Vector<Double> getB()
    {
        return this.b;
    }

    public VCalcCallback getCallback()
    {
        return this.callback;
    }

    public Double izvrsiSkalarniProizvod()
    {
        if(this.a.size() != this.b.size() || this.a == null || this.b == 0)
        {
            return null;
        }

        Double result = 0.0;
        int size = this.a.size();
        for(int i=0;i<size;i++)
        {
            rez += this.a[i] * this.b[i];
        }
    }
}

public interface CalcManager extends Remote
{
    public int SendVCalcRequest(VCalcRequest req) throws RemoteException;
    public boolean RunNextVCalc() throws RemoteException;
}

public class CalcManagerImpl extends UnicastRemoteObject implements CalcManager 
{
    private LinkedList<VCalcRequest> requests;//ovo je red cekanja
    private int ptr;//pokazivac na trenutni request

    public CalcManagerImpl() throws RemoteException
    {
        this.requests = new LinkedList<VCalcRequest>();
        this.ptr = 1;
    }

    public int SendVCalcRequest(VCalcRequest req) throws RemoteException
    {
        //preuzima zahtev za izracunavanje i stavlja ga u red cekanja 
        //pri tom generise i vraca id trenutnog izracunavanja

        if(req == null || req.getCallback() == null || req.getA() == null || req.getB() == null || req.getA().size()!=req.getB().size()) 
        {
            return -1;
        }

        VCalcRequest reqq = new VCalcRequest(this.ptr, req.getA(), req.getB(), req.getCallback());
        this.requests.addLast(reqq);
        this.ptr++;
        return this.ptr;
    }
    public boolean RunNextVCalc() throws RemoteException
    {
        //preuzima prvi zahtev sa liste cekanja, izvrsava operaciju skalarnog proizvoda
        //nad vektorima a i b, poziva odgovarajucu callback metodu i vraca true

        //hajde da uzmemo prvi zahtev iz liste cekanja
        VCalcRequest req = null;
        if(this.requests.size()>0)
        {
            req = this.requests.pollFirst();
        }
        else 
        {
            System.out.println("Nema requesta na cekanju!");
            return false;
        }

        if(req != null)
        {
            //onda smo nasli prvi zahtev iz liste cekanja
            result = req.izvrsiSkalarniProizvod();
            req.getCallback().onDone(req.getCid(), result);
            return true;
        }
        else return false;
    }

}

public interface VCalcCallback extends Remote
{
    public void onDone(int cid, Double result) throws RemoteException;
}

public class VCalcClient 
{
    private VCalcManager mngr;
    private VCalcCallback callback;
    
    public VCalcClient(String host, String port, String service)
    {
        this.mngr = (VCalcManager)Naming.Lookup("rmi://" + host + ":" + port + "/" + service);
        this.callback = new VCalcCallbackImpl();
    }

    public class VCalcCallbackImpl extends UnicastRemoteObject implements VCalcCallback
    {
        public VCalcCallbackImpl() throws RemoteException
        {
            super();
        }
        public void onDone(int cid, Double result) throws RemoteException
        {
            System.out.println("Za zatev ciji je CID: " + cid +", rezultat je: " +result);
        }
    }

    public VCalcManager getManager()
    {
        return this.mngr;
    }

    public VCalcCallback getCallback()
    {
        return this.callback;
    }

    public static void main(String[] args)
    {
        try
        {
            VCalcClient client = new VCalcClient("localhost", "1099", "VCalcService");
            Scanner scn = new Scanner(System.in);

            System.out.println("Pokrenut je sistem za izracunavanje skalarnog proizvoda dva vektora...");

            //kreirati jedan request i pozvati izracunavanje za njega
            for (int i = 0; i < 2; i++)
            {
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
                }, client.getCallback()));
            }

            client.getManager().RunNextVCalc();

            System.out.println("Enter za kraj...");

            scn.close();
                
        }
        catch(Exception e) { }
    }
}
