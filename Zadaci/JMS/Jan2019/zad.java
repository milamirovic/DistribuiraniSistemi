public class Stanica
{
    private String lokacija;

    private Topic tStigao;
    private Topic tUKvaru;

    private TopicConnection tc;
    private TopicSession ts;

    private TopicPublisher tPublisherStigao;
    private TopicSubscriber tSubscriberStigao;

    private TopicPublisher tPublisherUKvaru;
    private TopicSubscriber tSubscriberUKvaru;

    public Stanica(String lok)
    {
        this.lokacija = lok;

        InitialContext ictx = new InitialContext();

        TopicConnectionFactory tcf = (TopicConnectionFactory)ictx.lookup("tcfStanica");
        tStigao = (Topic)lookup("tStigao");
        tUKvaru = (Topic)lookup("tUKvaru");

        ictx.close();

        tc = (TopicConnection)tcf.createTopicConnection();
        ts = (TopicSession)tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);

        tc.start();

        //prima poruke od metode Stigao
        tSubscriberStigao = (TopicSubscriber) tc.createSubscriber(tStigao, "stanica LIKE '%" + this.lokacija + "%'");
        tSubscriberStigao.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Autobus je stigao u stanicu na lokaciji: " + this.lokacija);
            }
        });

        //prima poruke od UKvaru
        tSubscriberUKvaru = (TopicSubscriber)ts.createSubscriber(tUKvaru);
        tSubscriberUKvaru.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg)
            {
                System.out.println("Autobus " + msg.getStringProperty("autobus") + " je u kvaru!");
            }
        });
    }

    public void zatvori()
    {
        tc.close();
    }

    public void Stigao(String autobus, ArrayList<String> lokacijeStanica)
    {
        tPublisherStigao = (TopicPublisher) ts.createPublisher(tStigao);
        String poruka = "";
        for(String l: lokacijeStanica)
        {
            poruka += l + ";";
        }
        Message msg = ts.createMessage();
        msg.setStringProperty("stanica", poruka);
        msg.setStringProperty("autobus", autobus);
        tPublisherStigao.publish(msg);
        ts.commit();
    }

    public void UKvaru(String autobus)
    {
        tPublisherUKvaru = (TopicPublisher)ts.createPublisher(tUKvaru);
        Message msg = ts.createMessage();
        msg.setStringProperty("autobus", autobus);
        tPublisherUKvaru.publish(msg);
        ts.commit();
    }

    public static void main(String[] args) 
    {
        Scanner in = new Scanner(System.in);

        System.out.println("Unesite lokacuju stanice:");
        String lok = in.nextLine();

        Stanica s = new Stanica(lok);

        while(true)
        {
            System.out.println("Da li je autobus stigao u stanicu? (DA/NE/Exit)");
            String odg = in.nextLine();

            if(odg.equals("NE"))
            {
            }
            else if(odg.equals("DA"))
            {
                System.out.println("Unesite naziv autobusa:");
                String a = in.nextLine();
                System.out.println("Unesite stanice kroz koje voz prolazi:");
                ArrayList<String> stanice = new ArrayList<String>();

                while(true)
                {
                    System.out.println("Da li zelite da unesete stanicu? (DA/NE)");
                    String od = in.nextLine();
                    if(od.equals("NE"))
                    {
                        break;
                    }
                    System.out.println("Unesite lokaciju stanice:");
                    String st = in.nextLine();

                    if(!stanice.contains(st))
                    {
                        stanice.add(st);
                    }
                }

                s.Stigao(a, stanice);
            }
            else if(odg.equals("Exit"))
            {
                break;
            }
        }

        while(true)
        {
            System.out.println("Da li je autobus u kvaru? (DA/NE)");
            String ans = in.nextLine();
            if(ans.equals("NE"))
            {
                break;
            }
            System.out.println("Unesite naziv autobusa:");
            String autobus = in.nextLine();

            s.UKvaru(autobus);
        }

        s.zatvori();
    }
}
