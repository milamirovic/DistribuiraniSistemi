import java.rmi.registry.LocateRegistry;

/* OBJAVLJIVANJE PORUKA NA NIVOU TAGOVA */

public interface User extends Remote 
{
    public String getUsername() throws RemoteException;
    public void setUsername(String username) throws RemoteException;
    public int getId() throws RemoteException;
    public void setId(int id) throws RemoteException;
    public TagMessageCallback getCallback() throws RemoteException;
    public void setCallback(TagMessageCallback callback) throws RemoteException;
}
public class UserImpl extends UnicastRemoteObject implements User
{
    private int id;
    private String username;
    private TagMessageCallback callback;

    public UserImpl(int id, String username, TagMessageCallback callback) throws RemoteException
    {
        this.id = id;
        this.username = username;
        this.callback = callback;
    }

    public String getUsername() throws RemoteException
    {
        return this.username;
    }

    public void setUsername(String username) throws RemoteException
    {
        this.username = username;
    }

    public int getId() throws RemoteException
    {
        return this.id;
    }

    public void setId(int id) throws RemoteException
    {
        this.id = id;
    }

    public TagMessageCallback getCallback() throws RemoteException
    {
        return this.callback;
    }

    public void setCallback(TagMessageCallback callback) throws RemoteException
    {
        this.callback = callback;
    }
}

public class TagMessage 
{
    private UserImpl user;//kreator poruke, onaj user koji salje poruku
    private String message;//tekst poruke
    private List<String> tags;//hashtagovi poruke

    public TagMessage(UserImpl user, String message, List<String> tags)
    {
        this.user = user;
        this.message = message;
        this.tags = tags;
    }

    public void addTag(String tag)
    {
        this.tags.add(tag);
    }

    public String getMessage()
    {
        return this.message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public List<String> getTags()
    {
        return this.tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    public UserImpl getUser()
    {
        return this.user;
    }

    public void setUser(UserImpl user)
    {
        this.user = user;
    }
}

public interface TagManager extends Remote 
{
    public void sendMessage(TagMessage message) throws RemoteException;
    public void Follow(User user, String tag, TagMessageCallback cb) throws RemoteException;
}

public class TagManagerImpl extends UnicastRemoteObject implements TagManager
{
    private HashMap<String, User> UserFollowTag;

    public TagManagerImpl() throws RemoteException
    {
        this.UserFollowTag = new HashMap<String, User>();
    }

    public void sendMessage(TagMessage message) throws RemoteException
    {
        //salje novu poruku i poziva metod onTagMessage kod svih klijenata koji prate 
        //tagove koji su sadrzani u poruci message
        //metod onTagMessage(TagMessage msg, String tag) je definisan u okviru TagMessageCallback interfejsa

        //svi klijenti koji prate neki tag mogu da se izvuku iz hash tablice
        //nema potrebe da ih vadimo u neki niz, samo cemo svakom takvom useru da prikazemo poruku
        //za svaki tag u poruci message trazimo usere koji prate taj tag
        for(String tag: message.getTags()) 
        {
            for(User user: this.UserFollowTag.get(tag).values()) 
            {
                //svaka heshtablica ima elemente id i values, id je kod nas String - tag 
                //a values deo je user 
                //kad kazemo get(tag) onda se pribave svi elementi hashtablice koji za tag imaju bas ovaj dati tag
                //pa sa values() uzmemo samo usere iz tih elemenata

                //e sad, postoji user koji salje poruku i njemu ne bi trebalo da prosledimo tu poruku
                //prosto nema logike
                if(user.getId() != message.getUser().getId()) 
                {
                    //ovo je momenat kada zakljucujemo da mi preko callback metode obavestavamo
                    //USERA o nekoj poruci, pa je logicno da bas klasa User ima atribut tipa
                    //TagMessageCallback
                    //Iz tog razloga klasa User treba biti interface, pa se treba dodati posebna
                    //klasa UserImpl koja implementira ovaj interface
                    user.getCallback().onTagMessage(message, tag);
                }
            }
        }
    }

    public void Follow(User user, String tag, TagMessageCallback cb) throws RemoteException
    {
        //registruje korisnika tako da prati sve poruke koje sadrze dati tag
        //jedan korisnik moze da prati vise tagovania

        //da vidimo prvo da li neki user uopste prati ovaj tag
        if(this.UserFollowTag.get(tag) == null) 
        {
            //dakle nema usera koji prati dati tag, pa cemo samo dodati element u hashmapu
            //[tag user]
            this.UserFollowTag.put(tag, user);
            return;
        }
        else 
        {
            //da nije ovaj user vec zapratio dati tag?
            for(User u: this.UserFollowTag.get(tag).values())
            {
                if(u.getId() == user.getId())
                {
                    //znaci da ovaj user vec prati dati tag
                    System.out.println("User " + u.getUsername() + "sa id: " + u.getId() + " vec prati tag #" + tag);
                    return;
                }
            }
            //ako ovaj user ne prati dati tag, dodacemo to u hashmapu
            this.UserFollowTag.put(tag, user);
        }
    }
}

public interface TagMessageCallback
{
    public void onTagMessage(TagMessage message, String tag) throws RemoteException;
}

public class TagClinet 
{
    private TagManager manager;
    private TagMessageCallback callback;

    public TagClinet(String host, String port, String service) 
    {
        this.manager = (TagManager)Naming.Lookup("rmi://" + host + ":" + port + "/" + service);
        callback = new TagMessageCallbackImpl();
    }

    public void setManager(TagManager manager) 
    {
        this.manager = manager;
    }

    public void setCallback(TagMessageCallback callback)
    {
        this.callback = callback;
    }

    public TagManager getManager()
    {
        return this.manager;
    }

    public TagMessageCallback getCallback()
    {
        return this.callback;
    }

    public class TagMessageCallbackImpl extends UnicastRemoteObject implements TagMessageCallback
    {
        public TagMessageCallbackImpl() throws RemoteException
        {
            super();
        }
        public void onTagMessage(TagMessage message, String tag) throws RemoteException
        {
            System.out.println("Posiljalac: " + message.getUser().getUsername()); 
            System.out.println("Tekst poruke: " + message.getMessage());
            System.out.println("#" + tag);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            TagClinet client = new TagClinet("localhost", "1099", "TagMessageService");
            Scanner s = new Scanner(System.in);

            System.out.println("Odaberi jednu od opcija: ");
            System.out.println("1 - Posalji novi poruku");
            System.out.println("2 - Zaprati tag");
            System.out.println("3 - Izlaz");

            while(true) 
            {
                String opcija = s.nextLine().trim();
                if(opcija.equals("3"))
                {
                    System.out.println("Izlazak iz servisa...");
                    break;
                }

                System.out.println("Unesi svoj username:");
                String username = s.nextLine().trim();
                System.out.println("Unesi svoj id:");
                String id = s.nextLine().trim();
                User user = new UserImpl(username, id, client.getCallback());

                if(opcija.equals("1")) 
                {
                    System.out.println("Unesite tekst poruke: ");
                    String poruka = s.nextLine();
                    System.out.println("Unesite tagove poruke. Znak za kraj je 'exit'.");
                    String str = s.nextLine().trim();
                    ArrayList<String> tagovi = new ArrayList<String>();
                    while(!str.equals("exit")) 
                    {
                        tagovi.add(str);
                        str = s.nextLine().trim();
                    }
                    TagMessage msg = new TagMessage(User, poruka, tagovi);
                    client.getManager().sendMessage(msg);
                }
                else if(opcija.equals("2"))
                {
                    //zaprati tag
                    System.out.println("Unesti naziv taga koji zelite da zapratite:");
                    String tag = s.nextLine().trim();
                    client.getManager().follow(user, tag, client.getCallback());
                }
                else 
                {
                    System.out.println("Pogresan unos! Pokusajte ponovo!");
                }
            }
        }
        catch(RemoteException e)
        {
            System.out.println("Remote error: " + e.getMessage());
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

public class TagServer 
{
    private TagManager manager;

    public TagServer(String host, String port, String service)
    {
        LocateRegistry.createRegistry(Integer.parseInt(port));
        System.out.println("java rmi registry created.");
        manager = new TagManager();
        Naming.rebind("rmi://" + host + ":" + port + "/" + service, manager);
    }

    public TagManager getManager()
    {
        return this.manager;
    }

    public static void main(String[] args) 
    {
        try
        {
            TagServer server = new TagServer("loclhost", "1099", "TagMessageService");

            Scanner s = new Scanner(System.in);

            System.out.println("Servr ready. Double enter to end...");

            s.nextLine();
            s.nextLine();

            s.close();

            System.exit(0);
        }
        catch(RemoteException e)
        {
            e.printStackTrace();
        }
    }
}
