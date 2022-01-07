public class Recept
{
    private String naziv;
    private string text;

    public Recept(String naziv, String text)
    {
        this.naziv = naziv;
        this.text = text;
    }

    public Recept()
    {

    }

    public String getNaziv()
    {
        return this.naziv;
    }

    public String getText()
    {
        return this.text;
    }

    public void setNaziv(String naziv)
    {
        this.naziv = naziv;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}

public class Klijent
{
    private int id;
    private ArrayList<Recept> recepti;

    private Topic tId;
    private Queue qIdOdg;
    private Queue qRecept;
    private Queue qReceptOdg;

    private TopicPublisher tPublisherId;
    private TopicSubscriber tSubscriberId;

    private QueueSender qSenderIdOdg;
    private QueueReceiver qReceiverIdOdg;

    private QueueSender qSenderRecept;
    private QueueReceiver qReceiverRecept;

    private QueueSender qSenderReceptOdg;
    private QueueReceiver qReceiverReceptOdg;

    private TopicConnection tc;
    private QueueConnection qc;
    private TopicSession ts;
    private QueueSession qs;

    public Klijent(int id, ArrayList<Recpet> recepti)
    {
        this.id=id;
        this.recepti=recepti;

        InitialContext ictx = new InitialContext();

        TopicConnectionFactory tcf = (TopicConnectionFactory)ictx.lookup("tcfRecepti");
        QueueConnectionFactory qcf = (QueueConnectionFactory)ictx.lookup("qcfRecepti");
        tId = (Topic)ictx.lookup("tId");
        qIdOdg = (Queue)ictx.lookup("qIdOdg");
        qRecept = (Queue)ictx.lookup("qRecept");
        qReceptOdg = (Queue)ictx.lookup("qReceptOdg");

        ictx.close();

        tc = (TopicConnection) tcf.createTopicConnection();
        qc = (QueueConnection) qcf.createQueueConnection();

        ts = (TopicSession) tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
        qs = (QueueSession) qcf.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

        tc.start();
        qc.start();

        //svima salje svoj id
        tPublisherId = (TopicPublisher) ts.createPublisher(tId);
        Message msg = ts.createMessage();
        msg.setIntProperty("idNovi", this.id);
        tPublisherId.publish(msg);
        ts.commit();

        //primaju id novog klijenta i odgovaraju mu svojim id-em
        tSubscriberId = (TopicSubscriber)ts.createSubscriber(tId);
        qSenderIdOdg = (QueueSender)qs.createSender(qIdOdg);

        tSubscriberId.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Novi klijent " + msg.getIntProperty("idNovi"));
                msg.setIntProperty("idOdg", this.id);
                qSenderIdOdg.send(msg);
                qs.commit();
            }
        });

        //prima odgovore klijenata
        qReceiverIdOdg = (QueueReceiver) qs.createReceiver(qIdOdg, "idNovog = " + this.id);
        qReceiverIdOdg.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Odgovara klijent ", msg.getIntProperty("idOdg"));
            }
        });

        //prima zahtev za recept, ako ga ima salje mu taj recept
        for(Recept r: this.recepti)
        {
            qReceiverRecept = (QueueReceiver) qs.createReceiver(qRecept, "nazivRecepta = '" + r.getNaziv() + "'");
            qSenderReceptOdg = (QueueReceiver) qs.createSender(qReceptOdg);

            qReceiverRecept.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) 
                {
                    System.out.println("Klijent " + msg.getIntProperty("idTrazi") + " Vam trazi recept '" + msg.getStringProperty("nazivRecepta") + "'");
                    TextMessage tmsg = ts.createTextMessage(r.getText());
                    tmsg.setIntProperty("idTrazi", msg.getIntProperty("idTrazi"));
                    tmsg.setIntProperty("idSalje", this.id);
                    System.out.println("Salje se recept...");
                    qSenderReceptOdg.send(tmsg);
                    qs.commit();
                }
            });
        }
    }

    public void zatvori()
    {
        tc.close();
        qc.close();
    }

    public void preuzmiRecept(String nazivRecepta)
    {
        qSenderRecept = (QueueRecept)qs.createSender(qRecept);
        Message msg = qs.createMessage();
        msg.setIntProperty("idTrazi", this.id);
        msg.setStringProperty("nazivRecepta", nazivRecepta);
        qSenderRecept.send(msg);
        qs.commit();
    }

    public ArrayList<Recept> getRecepti() 
    {
        return this.recepti;
    }

    public static void main(String[] args) 
    {
        Scanner in = new Scanner(System.in);
        int id = getID();
        ArrayList<Recept> recepti = new ArrayList<Recept>();

        while(true)
        {
            System.out.println("Da li zelite da unesete novi recept? (DA/NE)");
            String odg = in.nextLine();

            if(odg.equals("NE"))
            {
                break;
            }
            else if(odg.equals("DA"))
            {
                System.out.println("Unesite naziv recepta: ");
                String naziv = in.nextLine();

                boolean flag = false;
                for(Recept r: recepti)
                {
                    if(r.getNaziv().equals(naziv))
                    {
                        System.out.println("Vec postoji recept sa istim imenom!");
                        flag = true;
                    }
                }
                if(!flag)
                {
                    System.out.println("Unesite tekst recepta: ");
                    String text = in.nextLine();
                    
                    recepti.add(new Recept(naziv, text));
                }
            }
            else System.out.println("Pogresan unos!");
        }

        Klijent k = new Klijent(id, recepti);

        while(true) 
        {
            System.out.println("Zelite li da zatrazite neki recept? (DA/NE)");
            String odg = in.nextLine();

            if(odg.equals("NE"))
            {
                break;
            }
            else if(odg.equals("DA"))
            {
                System.out.println("Unesite naziv zeljenog recepta:");
                String naziv = in.nextLine();
                k.preuzmiRecept(naziv);
                boolean flag = false;
                for(Recept rec: k.getRecepti())
                {
                    if(rec.getNaziv() == naziv)
                    {
                        flag = true;
                    }
                }
                if(!flag)
                {
                    System.out.println("Recept nije nadjen medju klijentima!");
                }
            }
            else System.out.println("Pogresan unos!");
        }
    }
}
