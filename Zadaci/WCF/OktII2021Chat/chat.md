```csharp
namespace WcfChat
{
    [ServiceContract(CallbackContract = typeof(IChatCallback), SessionMode = SessionMode.Required)]
    public interface IChat
    {
        [OperationContract]
        public string Login(string nickname);

        [OperationContract]
        public string PosaljiPoruku(string primalac, string tekstPoruke, strin posiljalac);

        [OperationContract]
        public List<ChatPoruka> IstorijaPoruka(DateTime od, DateTime do)
    }

    [DataContract]
    public class ChatPoruka
    {
        [DataMember]
        public string Primalac { get; set; } = "";

        [DataMember]
        public string Posiljalac { get; set; } = "";

        [DataMember]
        public DateTime Vreme { get; set; } = DateTime.Now;

        [DataMember]
        public string Poruka { get; set; } = "";

        public ChatPoruka()
        {

        }

        public ChatPoruka(string primalac, string msg, DateTime time, string posiljalac)
        {
            Primalac = primalac;
            Vreme = time;
            Poruka = msg;
            Posiljalac = posiljalac;
        }
    }
}

namespace WcfChat 
{
    [ServiceBehaviour(InstanceContextMode = InstanceContextMode.Single)]
    public class Chat : IChat
    {
        private List<ChatPoruke> chatPoruke = new List<ChatPoruke>();

        private Dictionary<string, IChatCallback> nicknameCallbacks = new Dictionary<string, IChatCallback>();

        public string Login(string nickname)
        {
            IChatCallback callback = OperationContext.Current.GetCallbackChannel<IChatCallback>();
            if(!nicknameCallbacks.ContainsKey(nickname))
            {
                nicknameCallbacks.Add(nickname, callback);
                return "Uspesan login!";
            }
            else 
            {
                //vec postoji taj user u sistemu
                //pise da ako se registruje isti nickname, smatrati da je prethodna sesija prestala da vazi
                return "Vec ulogovan korisnik!";
            }
        }

        public string PosaljiPoruku(string primalac, string tekstPoruke, string posiljalac)
        {
            ChatPoruka chatPoruka = new ChatPoruka(primalac, DateTime.Now, tekstPoruke, posiljalac);
            if(primalac == "SVI")
            {
                //ako je namenjena svima, stize trenutno aktivnim userima
                List<string> aktivniUseri = new List<string>();
                foreach(KeyvaluePair<string, IChatCallback> entry in nicknameCallbacks)
                {
                    aktivniUseri.Add(entry.Key);
                }
                //sada imamo listu nickname-ova svih aktivnih usera
                foreach(string s in aktivniUseri)
                {
                    IChatCallback callback = nicknameCallbacks[s];//uzmemo value deo iz dict
                    callback.PosaljiPorukuCallback(chatPoruka);
                }
            }
            else 
            {
                //onda je naveden username konkretnog usera
                if(nicknameCallbacks.ContainsKey(primalac))
                {
                    //ako ga ima medju registrovanima onda mu se salje
                    IChatCallback callback = nicnameCallbacks[primalac];
                    callback.PosaljiPorukuCallback(chatPoruka);
                }
                else 
                {
                    //tog usera nema medju registrovanima...
                    return "Primalac nije registrovan!";
                }
            }
            //dodaj u istoriju poruka:
            chatMessages.Add(chatPoruka);
            return "Poruka poslata!";
        }

        public List<ChatPoruka> IstorijaPoruka(DateTime od, DateTime do) 
        {
            List<ChatPoruka> messages = new List<ChatPoruka>()''
            foreach(ChatPoruka c in this.chatPoruke)
            {
                if(c.Vreme >= od && c.Vreme <= do)
                {
                    messsages.Add(c);
                }
            }
            return messages;
        }
    }
}

namespace WcfChat
{
    public interface IChatCallback
    {
        [OperationContract(IsOneWay = true)]
        public void PosaljiPorukuCallback(ChatPoruka chatPoruka);
    }
}

namespace Klijent
{
    public class Form1: Form, IChatCallback
    {
        private ChatClient proxy;
        private string nickname;
        private bool ulogovan = false;

        public Form1()
        {
            InitializeComponent();
            ulogovan = false;
            proxy = new ChatClient(InstanceContext(this));
        }

        //implementacija od IChatCallback
        public void PosaljiPorukuCallback(Chat chatPoruka)
        {
            lblPrikaziPoruku.Text = chatPoruka.Poruka;
            lblVreme.Text = chatPoruka.Vreme;
            lblPosiljalac.Text = chatPoruka.Posiljalac;
        }

        //eventi sa dugmetom

        private void btnLogin_Click(object sender, EventArgs e)
        {
            ulogovan = true;
            string nickname = txtNicknameLogin.Text;
            this.nickname = nickname;
            lblLoginOdgovor.Text = proxy.login(nickname);
        }

        private void btnSaljiPorukuSvima_Click(object sender, EventArgs e)
        {
            if(ulogovan)
            {
                string poruka = txtTekstPoruke.Text;
                //salje se svima
                proxy.PosaljiPoruku("SVI", poruka, this.nickname);
                lblPorukaPoslata.Text = "Poruka je poslata";
            }
            else
            {
                lblPorukaPoslata.Text = "Prvo se ulogujte!";
            }
        }

        private void btnSaljiPorukuDrugomUseru(object sender, EventArgs e)
        {
            if(ulogovan)
            {
                string poruka = txtTekstPoruke.Text;
                string primalac = txtPrimalac.Text;
                lblPorukaPoslata.Text = proxy.PosaljiPoruku(primalac, poruka, this.nickname);
            }
            else
            {
                lblPorukaPoslata.Text = "Prvo se ulogujte!";
            }
        }

        private void btnPrikaziIstoriju(object sender, EventArgs)
        {
            DateTime od = dtp1.Value;
            DateTime do = dtp2.Value;

            List<ChatPoruka> istorija = proxy.IstorijaPoruka(od, do);

            String zaPrikaz = "Istorija poruka: \n";
            foreach(ChatPoruka cp in istorija)
            {
                if(cp.Primalac == this.nickname || cp.Primalac == "SVI")
                {
                    //znaci on je primalac
                    zaPrikaz = zaPrikaz + "posiljalac: " + cp.Posiljalac + " vreme: " + cp.Vreme + " poruka: " + cp.Poruka + "primalac: " + cp.Primalac + "\n";
                }
            }

            lblIstorija.Text = zaPrikaz;
        }
    }
}
```

```xml
<system.serviceModel>
    <services>
        <service name = "WcfChat.Chat">
            <endpoint 
                binding = "wsDualHttpBinding"
                address = ""
                contract = "IChat"
            />
        </service>
    </services>
</system.serviceModel>
```
