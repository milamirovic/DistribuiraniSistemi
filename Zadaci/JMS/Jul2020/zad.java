public class Klijent
{
    private int id;
    private ArrayList<String> blanketi;

    private Topic tId;

    private TopicPublisher tPublisherId;
    private TopicSubscriber tSubscriberId;

    private Queue qIdOdg;

    private QueueSender qSenderIdOdg;
    private QueueReceiver qReceiverIdOdg;

    private Queue qBlanket;

    private QueueSender qSenderBlanket;
    private QueueReveiver qReceiverBlanket;

    private Queue qBlanketOdg;

    private QueueSender qSenderBlanketOdg;
    private QueueReceiver qReceiverBlanketOdg;

    public Klijent(int id, ArrayList<String> blanketi)
    {
        this.id = id;
        this.blanketi = blanketi;

        InitialContext ictx = new InitialContext();

        TopicConnectionFactory tcf = (TopicConnectionFactory)ictx.lookup("tcfBlanketi");
        QueueConnectionFactory qcf = (QueueConnectionFactory)ictx.lookup("qcfBlanketi");
        tId = (Topic)ictx.lookup("tId");
        qIdOdg = (Queue)ictx.lookup("qIdOdg");
        qBlanket = (Queue)ictx.lookup("qBlanket");
        qBlanketOdg = (Queue)ictx.lookup("qBlanketOdg");

        ictx.close();

        tc = (TopicConnection)txf.createTopicConnection();
        qc = (QueueConnection)qcf.createQueueConnection();

        ts = (TopicSession)tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
        qs = (QueueSession)qc.createQueueSession(ture, Session.AUTO_ACKNOWLEDGE);

        tc.start();
        qc.start();

        //Salje id
        tPublisherId = (TopicPublisher)ts.createPublisher(tId);
        Message msg = ts.createMessage();
        msg.setIntProperty("idNovi", this.id);
        tPublisherId.publish(msg);
        ts.commit();

        //Prima i odgovara sa svojim id
        tSubscriberId = (TopicSubscriber)ts.createSubscriber(tId);
        qSenderIdOdg = (QueueSender)qs.createSender(qIdOdg);
        tSubscriberId.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Novi klijent: " + msg.getIntProperty("idNovi"));
                msg.setIntProperty("idOdg", this.id);
                qSenderIdOdg.send(msg);
                qs.commit();
            }
        });

        //Primi odgovor za id
        qReceiverIdOdg = (QueueReceiver)qs.createReceiver(qIdOdg, "idNovi=" + this.id);
        qReceiverIdOdg.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Odgovara klijent " + msg.getIntProperty("idOdg"));
            }
        });

        //primi zahtev za blanketom i posalje taj blanket
        qSenderBlanketOdg = (QueueSender)qs.createSender(qBlanketOdg);
        for(String b: this.blanketi)
        {
            qReceiverBlanket = (QueueReceiver)qs.createReceiver(qBlanket, "blanket='" + b + "'");
            qReceiverBlanketOdg.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) 
                {
                    System.out.println("Klijent " + msg.getIntProperty("idTrazi") + " Vam trazi blanket '" + msg.getStringProperty("blanket") + "'");
                    msg.setIntProperty("idDaje", this.id);
                    qSenderBlanketOdg.send(msg);
                    qs.commit();
                }
            });
        }

        //primi blanket odgovor
        qReceiverBlanketOdg = (QueueReceiver)qs.createReceiver(qBlanketOdg, "idTrazi="+this.id);
        qReceiverBlanketOdg.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Blanket " + msg.getStringProperty("blanket") + " Vam salje klijent " + msg.getIntProperty("idDaje"));
                this.blanketi.add(msg.getStringProperty("blanket"));
            }
        });
    }

    public void zatvori()
    {
        tc.close();
        qc.close();
    }

    public void preuzmi(String blanket)
    {
        if(this.blanketi.contains(blanket))
        {
            System.out.println("Vec posedujete dati blanket...");
            return;
        }
        qSenderBlanket = (QueueSender)qs.createSender(qBlanket);
        Message msg = qs.createMessage();
        msg.setIntProperty("idTrazi", this.id);
        msg.setStringProperty("blanket", blanket);
        qSenderBlanket.send(msg);
        qs.commit();
    }

    public ArrayList<String> getBlanketi() 
    {
        return this.blanketi;
    }

    public static void main(String[] args) 
    {
        Scanner in = new Scanner(System.in);
        int id = getID();
        ArrayList<String> blanketi = new ArrayList<String>();

        while(true)
        {
            System.out.println("Zelite li da unesete blanket? (da/ne)");
            String odg = in.nextLine();

            if(odg.equals("ne"))
            {
                break;
            }
            else if(odg.equals("da"))
            {
                System.out.println("Unesite naziv blanekta:");
                String bl = in.nextLine();
                if(!blanketi.contains(bl))
                {
                    blanketi.add(bl);
                }
            }
            else System.out.println("Pogresan unos!");
        }

        Klijent k = new Klijent(id, blanketi);

        while(true)
        {
            System.out.println("Da li zelite da preuzmete neki blanket? (da/ne)");
            String odg = in.nextLine();
            if(odg.equals("ne"))
            {
                break;
            }
            else if(odg.equals("da"))
            {
                System.out.println("Unesite naziv blanketa:");
                String bl = in.nextLine();

                k.preuzmi(bl);

                if(!k.getBlanketi().contains(bl))
                {
                    System.out.println("Nije pronadjen dati blanket!");
                }
            }
        }

        k.zatvori();
        in.next();
        System.exit(0);
    }
}
