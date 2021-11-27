# Jun 2021 - RASPRODAJA

```java
public class Prodavnica
{
    public String naziv;
    public int popust;

    public Prodavnica(String naziv, int popust)
    {
        this.naziv = naziv;
        this.popust = popust;
    }

    public Prodavnica()
    {

    }
}
```

```java
public class Klijent
{
    private ArrayList<Prodavnica> prodavnice;

    private TopicPublisher tPublisherRasprodaje;
    private TopicSubscriber tSubscriberRasprodaje;

    private TopicConnection tc;
    private TopicSession ts;

    public Klijent(ArrayList<Prodavnice> prodavnice)
    {
        this.prodavnice = prodavnice;

        InitialContext ictx = new InitialContext();
        TopicConnectionFactory tcf = (TopicConnectionFactory)ictx.lookup("tcfRasprodaja");
        Topic tRasprodaja = (Topic)ictx.lookup("tRasprodaja");
        
        ictx.close();

        tc = (TopicConnection) tcf.createConnection();
        ts = (TopicSession) tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);

        tPublisherRasprodaje = (TopicPublisher) ts.createPublisher(tRasprodaja);
        //tSubscriberRasprodaje = (TopicSubscriber) ts.createSubscriber(tRasprodaja);

        for(Prodavnica p: this.prodavnice)
        {
            Message msg = ts.createMessage();
            msg.setStringProperty("prodavnica", p.naziv);
            msg.setIntProperty("popust", p.popust);

            tPublisherRasprodaje.send(msg);

            ts.commit();
        }

        for(Prodavnica p: this.prodavnice) 
        {
            //dakle, ako se primljeno obavestenje o rasprodaji odnosi na neku od prodavnica koje interesuju ovog klijenta
            //onda cemo primiti to obavestenje
            Rasprodaja(p);
        }
    }

    public void Rasprodaja(Prodavnica p)
    {
        //salje ostalima u kojoj prodavnici je trenutno rasprodaja i sa kojim popustom
        tSubscriberRasprodaje = (TopicSubscriber) ts.createSubscriber(tRasprodaja, "prodavnica = '" + p.naziv, true);
        tSubscriberRasprodaje.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                String naziv = msg.getStringProperty("prodavnica");
                Integer popust = msg.getIntegerProperty("popust");

                System.out.println("Prodavnica " + naziv + " ima rasprodaju od " + popust +"% popusta!");
            }
        });
    }

    public void ZatvoriKonekciju()
    {
        tc.close();
    }

    public static void main(String[] args)
    {
        Scanner in = new Scanner(System.in);

        ArrayList<Prodavnica> listaProdavnica = new ArrayList<Prodavnica>();

        System.out.println("Da li zelite da zapratite novu prodavnicu? (DA ili NE)");
        String odg = in.nextLine();

        while(odg == "DA")
        {
            System.out.println("Unesite naziv prodavnice: ");
            String ime = in.nextLine();
            Integer discount;
            Integer flag = 0;//ako postane 1 znaci da je prodavnica vec uneta!
            Prodavnica novaProdavnica = new Prodavnica();

            for(Prodavnica p: listaProdavnica)
            {
                if(p.naziv == ime)
                {
                    flag = 1;
                    System.out.println("Ova prodavnica je vec zapracena i u Vasoj je listi. Unesite novu vrednost za popust:");
                    discount = Integer.parseInt(in.nextLine().trim());
                    if(discount>=5 && discount<=100)
                    {
                        p.popust = discount;
                        System.out.println("Popust azuriran.");
                    }
                    else
                    {
                        System.out.println("Nevalidan unos popusta! Popust ostaje pri staroj vrednosti " + p.popust + "%!");
                    }
                    break;//da ne trazi dalje, ako ga je nasao
                }
            }

            if(flag == 0)
            {
                novaProdavnica.naziv = ime;
                System.out.println("Unesti vrednost popusta: ");
                discount = Integer.parseInt(in.nextLine().trim());
                if(discount>=5 && discount<=100)
                {
                    novaProdavnica.popust = discount;
                }
                else
                {
                    while(discount<5 || discount>100)
                    {
                        System.out.println("Pogresan unos popusta! Pokusajte ponovo: ");
                        discount = Integer.parseInt(in.nextLine());
                    }
                    novaProdavnica.popust = discount;
                }
                listaProdavnica.add(novaProdavnica);
            }
        }

        Klijent ana = new Klijent(listaProdavnica);

        in.next();

        ana.ZatvoriKonekciju();

        System.exit(0);
    }
}
```
