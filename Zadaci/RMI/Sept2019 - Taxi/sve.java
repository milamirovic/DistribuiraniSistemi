import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class TaxiServer
{
    public String rmiString;
    public int port;

    public TaxiServer(String host, String port, String service)
    {
        this.port = Integer.parseInt(port);
        this.rmiString = "rmi://" + host + ":" + port + "/" + service;
    }
    public static void main(String[] args) 
    {
        try
        {
            TaxiServer server = new TaxiServer("localhost", "1099", "TaxiService");
            LocateRegistry.createRegistry(server.port);
            System.out.println("java RMI registry created.");

            TaxiManager mngr = new TaxiManagerImpl();
            Naming.rebind(server.rmiString, mnger);
        }
        catch(RemoteException e) { System.out.println("Error: " + e); }
    }
}

public class Taxi implements Serializable
{
    private int id;
    private String address;
    private boolean isFree;

    private TaxiCallback callback;

    public Taxi(int id, String adr, boolean free, TaxiCallback callback)
    {
        this.id = id;
        this.address = adr;
        this.isFree = free;
        this.callback = callback;
    }

    public int getId()
    {
        return this.id;
    }

    public String getAddress()
    {
        return this.address;
    }

    public boolean isFree()
    {
        return this.isFree;
    }

    public TaxiCallback getCallback()
    {
        return this.callback;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public void setIsFree(boolean isFree)
    {
        this.isFree = isFree;
    }

    public void setCallback(TaxiCallback callback)
    {
        this.callback = callback;
    }
}

public interface TaxiManager extends Remote 
{
    public boolean requestTaxi(String address) throws RemoteException;
    public void setTaxiStatus(int id, bool isFree) throws RemoteException;
    public void dodajTaxi(int id, String addr, boolean free) throws RemoteException;
}

public class TaxiManagerImpl extends UnicastRemoteObject implements TaxiManager
{
    private LinkedList<Taxi> taxiji;
    private LinkedList<String> adreseKorisnika;
    private int kapacitet;

    public TaxiManagerImpl(int kap) throws RemoteException
    {
        this.taxiji = new LinkedList<Taxi>();
        this.adreseKorisnika = new LinkedList<String>();
        this.kapacitet = kap;
    }

    public boolean requestTaxi(String address) throws RemoteException 
    {
        //prvom slobodnom vozilu dodeljuje adresu i vraca true (Round Robin princip)
        //ako ne postoji slobodno vozilo i ako postoji prazno mesto u redu cekanja 
        //onda ubacuje adresu u red cekanja  i vraca true dok u suprotnom vraca false

        //hajde da nadjemo prvi slobodni auto
        Taxi slobodan = null;
        for(Taxi t: this.taxiji)
        {
            if(t.isFree())
            {
                slobodan = t;
                //round robin deo
                this.taxiji.remove(t);
                this.taxiji.addLast(t);
                break;
            }
        }

        //ako je nadjen slobodan taxi onda on nije null
        if(slobodan!=null)
        {
            //onda je nadjen sobodan taxi
            slobodan.setIsFree(false);//vise nije slobodan
            slobodan.getCallback().notifyTaxi(address);
            return true;
        }
        else if(this.adreseKorisnika.size()< this.kapacitet)
        {
            //ako nema slobodnih taxija, onda ako red za cekanje ima jos mesta onda dodaj
            //u listu cekanja tu adresu

            this.adreseKorisnika.addLast(address);
            return true;
        }
        else return false;
    }

    public void setTaxiStatus(int id, boolean isFree) throws RemoteException
    {
        //postavlja status vozila sa datim id-jem 
        //ako je vozilo postalo slobodno i ako postoje adrese u redu cekanja
        //potrebno mu je automatski dodeliti adresu iz reda cekanja 
        Taxi taxi = null;
        for(Taxi t: this.taxiji)
        {
            if(t.getId()==id)
            {
                taxi=t;
                break;
            }
        }

        //ako je nadjen taj taxi onda vise nije null
        if(taxi!=null)
        {
            //onda je nadjen taxi sa datim id-em
            if(isFree==false)
            {
                //dakle postavljamo da taxi nije slobodan
                taxi.setIsFree(isFree);
            }
            else
            {
                //ako treba da postavimo taxi na status slobodno
                //onda ako postoje adrese u redu cekanja potrebno mu je automatski
                //dodeliti adresu iz reda cekanja  

                taxi.setIsFree(isFree);
                if(this.adreseKorisnika.size()>0) 
                {
                    String adresa = this.adreseKorisnika.pollFirst();
                    if(adresa!=null)
                    {
                        //pollFirst() uzme prvi element i izbaci ga iz liste
                        //vraca null ako nema nicega
                        taxi.setIsFree(false);//sad je zauzet
                        taxi.getCallback().notifyTaxi(adresa);
                    }
                } 
            }
        }
    }

    public void dodajTaxi(int id, String adddr, boolean free) throws RemoteException 
    {
        int taxiID = this.taxiji.size();
        Taxi taxi = new Taxi(taxiID, addr, free);
        this.taxiji.addLast(taxi);
    }
}

public interface TaxiCallback extends Remote 
{
    public void notifyTaxi(String address) throws RemoteException;
}

public class TaxiDriverClient 
{
    private TaxiManager mngr;
    private TaxiCallback cb;
    public int port;
    public String rmiString;

    public TaxiDriverClient(String host, String port, String service)
    {
        this.port = Integer.parseInt(port);
        this.rmiString = "rmi://" + host + ":" + port + "/" + service;
        this.mngr = (TaxiManager)Naming.Lookup(rmiString);
        cb = new TaxiCallbackImpl();
    }

    public TaxiManager getTaxiManager()
    {
        return this.mngr;
    }

    public TaxiCallback getCallback()
    {
        return this.cb;
    }

    public void setManager(TaxiManager manager)
    {
        this.mngr = manager;
    }

    public void setCallback(TaxiCallback callback)
    {
        this.cb = callback;
    }

    public class TaxiCallbackImpl extends UnicastRemoteObject implements TaxiCallback
    {
        public TaxiCallbackImpl() throws RemoteException { super(); }
        public void notifyTaxi(String address) throws RemoteException
        {
            System.out.println("Klijent je na adresi: '" + address + "'");
        }
    }

    public static void main(String[] args)
    {
        try
        {
            TaxiDriverClient driver = new TaxiDriverClient("localhost", "1099", "TaxiService");
            Scanner scn = new Scanner(System.in);

            System.out.println("Unesite Vasu adresu: ");
            String adrDrivera = scn.nextLine();
            driver.getManager().dodajTaxi(-1, adrDrivera, true);

            scn.close();
        }
        catch(RemoteException e) { }
        catch(Exception e) { }
    }
}

public class TaxiUserClient
{
    private TaxiManager mngr;
    //private TaxiCallback cb;
    public int port;
    public String rmiString;

    public TaxiUserClient(String host, String port, String service)
    {
        this.port = Integer.parseInt(port);
        this.rmiString = "rmi://" + host + ":" + port + "/" + service;
        this.mngr = (TaxiManager)Naming.Lookup(rmiString);
        //this.cb = new TaxiCallbackImpl();
    }

    public static void main(String[] args) 
    {
        try
        {
            TaxiUserClient user = new TaxiUserClient("localhost", "1099", "TaxiService");
            Scanner scn = new Scanner(System.in);

            System.out.println("Na koju adresu zelite da taxi dodje?");
            String adresaUsera = scn.nextLine();

            user.getManager().requestTaxi(adresaUsera);

            scn.close();
        }
        catch(RemoteException e) { }
        catch(Exception e) { }
    }
}