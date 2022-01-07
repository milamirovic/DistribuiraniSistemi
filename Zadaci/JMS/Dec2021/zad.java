public class Klijent 
{
    private int x;
    private int y;
    private double koef;

    //ako je xy sistem, reka tece od juga ka severu to je u pozitivnom pravcu y ose 
    //a ako tece od zapada ka istoku, to je u pozitivnom pravcu x ose
    //dakle, tacka na kojoj sam treba da ima manje y ili manje x koordinate od one nizvodno

    private Topic tProsledi;
    private Queue qInfos;

    private TopicConnection tc;
    private QueueConnection qc;
    private TopicSession ts;
    private QueueSession qs;

    private TopicPublisher tPublisherProsledi;
    private TopicSubscriber tSubscriberProsledi;

    private QueueSender qSenderInfos;
    private QueueReceiver qReceiverInfos;

    public Klijent(int x, int y, double zag)
    {
        this.x = x;
        this.y = y;
        this.koef = zag;

        InitialContext ictx = new InitialContext();
        TopicConnectionFactory tcf = (TopicConnectionFactory)ictx.lookup("tcfZagadjenje");
        QueueConnectionFactory qcf = (QueueConnectionFactory)ictx.lookup("qcfZagadjenje");
        tProsledi = (Topic)ictx.lookup("tProsledi");
        qInfos = (Queue)ictx.lookup("qInfos");
        ictx.close();

        tc = (TopicConnection)tcf.createTopicConnection();
        qc = (QueueConnection)qcf.createTopicConnection();

        ts = (TopicSession)tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
        qs = (QueueSession)tc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

        tc.start();
        qc.start();

        //primi poruku od Prosledi fje
        tSubscriberProsledi = (TopicSubscriber)ts.createSubscriber(tProsledi, "x < " + this.x + " OR y < " + this.y);
        tSubscriberProsledi.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Klijent [" +msg.getIntProperty("x") + ", " + msg.getIntProperty("y") +"] Vam prosledjuje koeficijent zagadjenja: " + msg.getDoubleProperty("koef"));
                this.koef = msg.getDoubleProperty("koef");
            }
        });

        //primi poruku namenjenu samo jednom klijentu nizvodno
        qReceiverInfos = (QueueReceiver)qs.createReceiver(qInfos, "x < " + this.x + " OR y < " + this.y);
        qReceiverInfos.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Klijent [" + msg.getIntProperty("x") + ", " + msg.getIntProperty("y") + "] Vam salje poslednje poznato zagadjenje: " + msg.getDoubleProperty("koef"));
                this.koef = msg.getDoubleProperty("koef");
            }
        });
    }

    public zatvori() 
    {
        tc.close();
        qc.close();
    }

    public void prosledi(dobule koeficijent)
    {
        tPublisherProsledi = (TopicPublisher)ts.createPublisher(tProsledi);
        Message msg = ts.createMessage();
        msg.setIntProperty("x", this.x);
        msg.setIntProperty("y", this.y);
        msg.setDoubleProperty("koef", koeficijent);
        this.koef = koeficijent;
        tPublisherProsledi.publish(msg);
        ts.commit();
    }

    public void posalji()
    {
        qSenderInfos = (QueueSender)qs.createSender(qInfos);
        Message msg = t.createMessage();
        msg.setIntProperty("x", this.x);
        msg.setIntProperty("y", this.y);
        msg.setDoubleProperty("koef", this.koef);
        qSenderInfos.send(msg);
        qs.commit();
    }

    public static void main(String[] args) 
    {
        Scanner in = new Scanner(System.in);
        System.out.println("Unesite x koordinatu:");
        int x = in.nextInt();

        System.out.println("Unesite y koordinatu:");
        int y = in.nextInt();

        System.out.println("Unesite trenutni koeficijent zagadjenja reke:");
        double koef = in.nextDouble();

        Klijent a = new Klijent(x, y, koef);

        while(true) 
        {
            System.out.println("Da li zelite da prosledite koeficijent zagadjenja nizvodno? (da/ne)");
            String odg = in.nextString();

            if(odg.equals("ne"))
            {
                break;
            }
            else if(odg.equals("da"))
            {
                System.out.println("Unesite trenutni koeficijent zagadjenja reke: ");
                double k = in.nextDouble();

                a.prosledi(k);
            }
            else System.out.println("Pogresan unos!");
        }

        while(true)
        {
            System.out.println("Da li zelite da posaljete poslednje poznato zagadjenje prvom klijentu nizvodno? (da/ne)");
            String odg = in.nextString();

            if(odg.equals("ne"))
            {
                break;
            }
            else if(odg.equals("da"))
            {
                a.posalji();
            }
            else System.out.println("Pogresan unos!");
        }

        a.zatvori();
        in.next();
        System.exit(0);
    }
}
