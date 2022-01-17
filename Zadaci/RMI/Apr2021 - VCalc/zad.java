public class VCalcRequest implements Serializable
{
    private int cId;
    private Vector<Double> a;
    private Vector<Double> b;
    private VCalcCallback cb;

    public VCalcRequest(int cId, Vector<Double> a, Vector<Double> b, VCalcCallback cb) 
    {
        super();
        this.cId = cId;
        this.a = a;
        this.b = b;
        this.cb = cb;
    }

    public void setCId(int cId)
    {
        this.cId = cId;
    }

    public void setA(Vector<Double> a)
    {
        this.a = a;
    }

    public void setB(Vector<Double> b)
    {
        this.b = b;
    }

    public void setCb(VCalcCallback cb)
    {
        this.cb = cb;
    }

    public int getCId()
    {
        return this.cId;
    }

    public Vector<Double> getA()
    {
        return this.a;
    }

    public Vector<Double> getB()
    {
        return this.b;
    }

    public VCalcCallback getCb()
    {
        return this.cb;
    }

    public Dobule operacija()
    {
        if(this.a == null || this.b || this.a.size() != this.b.size())
        {
            return null;
        }

        Double rez = 0.0;
        for(int i=0;i<a.size();i++)
        {
            rez += this.a.get(i) * this.b.get(i);
        }

        return rez;
    }
}

public interface CalcManager extends Remote
{
    public int SendVCalcRequest(VCalcRequest req) throws RemoteException;
    public boolean RunNextVCalc() throws RemoteException;
}

public class CalcManagerImpl extends UnicastRemoteObject implements CalcManager
{
    private LinkedList<VCalcRequest> requests;

    public CalcManagerImpl() throws RemoteException
    {
        super();
        this.requests = new LinkedList<VCalcRequest>();
    }

    public int SendVCalcRequest(VCalcRequest req) throws RemoteException
    {
        //preuzima zahtev za izracunavanjem i stavlja ga u red cekanja, pri tome 
        //generise se i vraca jedinstveni identifikator trenutnog izracunavanja

        if(req == null || req.getCb() == null || req.getA() == null || req.getB() == null || req.getA().size() != req.getB().size()) 
        {
            return -1;
        }

        VCalcRequest newReq = new VCalcRequest(this.requests.size(), req.getA(), req.getB(), req.getCb());
        this.requests.addLast(newReq);//tako se poveca i size za 1
        return this.requests.size();
    }

    public boolean RunNextVCalc() throws RemoteException
    {
        //preuzima prvi zahtev iz reda cekanja, izvrsava zadatu operaciju nad vektorima
        //i poziva odgovarajuci callback metodu i vraca true
        VCalcRequest tmp = this.requests.pollFirst();//uzme prvog i izbaci ga iz liste
        Double rez = tmp.operacija();
        tmp.getCb().onDone(tmp.getCId(), rez);
    }
}

public interface VCalcCallback extends Remote
{
    public ovid onDone(int cId, double result) throws RemoteException;
}

public class VCalcServer 
{
    private VCalcManager mngr;

    public VCalcServer(String host, String port, String service) 
    {
        LocateRegistry.createRegistry(Integer.parseInt(port));
        System.out.println("Java rmi registry created.");

        this.mngr = new VCalcManager();
        Naming.rebind("rmi://" + host + ":" + port + "/" + service, mngr);
    }

    public static void main(String[] args) 
    {
        try
        {
            VCalcServer server = new VCalcServer("localhost", "1099", "VCalcService");

            Scanner s = new Scanner(System.in);

            System.out.println("Server ready...");

            s.nextLine();
            s.nextLine();
        }
        catch(RemoteException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }
}

public class VCalcClient 
{
    public VCalcManager mngr;
    private VCalcCallback cb;

    public VCalcClient(String host, String port, String service)
    {
        this.mngr = (VCalcManager) Naming.lookup("rmi://" + host + ":" + port + "/" + service);
        this.cb = new VCalcCallbackImpl();
    }

    public VCalcCallback getCb() 
    {
        return this.cb;
    }

    public class VCalcCallbackImpl extends UnicastRemoteObject implements VCalcCallback
    {
        public VCalcCallbackImpl()
        {
            super();
        }
        public void onDone(int cid, Double result) throws RemoteException
        {
            System.out.println("CID: " + cid);
            System.out.println("result: " + result);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            VCalcClient client = new VCalcClient("localhost", "1099", "VCalcService");
            Scanner s = new Scanner(System.in);

            Vector<Double> a = new Vector<Double>();
            Vector<Double> b = new Vector<Double>();

            while(true) 
            {
                System.out.println("Zelite li da izvrsite novo izracunavanje? (DA/NE)");
                String odg = s.nextLine();
                if(s.equals("NE"))
                {
                    break;
                }
                while(true)
                {
                    System.out.println("Unesite novi element vektora a: (otkucajte END za kraj)");
                    String aa = s.nextLine();
                    System.out.println("Unesite novi element vektora b: (otkucajte END za kraj)");
                    String bb = s.nextLine();
                    if(aa.equals("END") || aa.equals("end") || bb.equals("END") || bb.equals("end")) 
                    {
                        break;
                    }
                    a.add(Double.parseDouble(aa));
                    b.add(Double.parseDouble(bb));
                }
                VCalcRequest vc = null;
                if(a.size()!=b.size() || a.size()==0)
                {

                }
                else 
                {
                    vc = new VCalcRequest(-1, a, b, client.getCb());
                    int id = client.mngr.SendVCalcRequest(vc);
                    System.out.println("ID request: " + id);
                    if(client.mngr.RunNextVCalc())
                    {
                        System.out.println("RunNextVCalc je uspesno izvrsena!");
                    }
                }
            }

            System.out.println("Pritisnikte enter za kraj...");
            s.nextLine();
            s.close();
            System.exit(0);
        }
        catch(RemoteException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }
}
