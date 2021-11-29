```csharp
/* KALKULATOR U KOME SE PAMTI NADIMAK KLIJENTA TJ VLASNIKA KALKULATORA */
//na snimku 11. DS-R [31.5.2021] prica o tome malo od 1:12:00
//uradicu samo sa DODAJ(d)

namespace WcfKalkulator
{
    [ServiceContract(CallbackContract = typeof(IKalkulatorCallback), SessionMode = SessionMode.Required)]
    public interface IKalkulator
    {
        [OperationContract(IsOneWay = true)]
        public void Dodaj(decimal d);
    }

    [DataConstract]
    public class Rezultat
    {
        decimal vrednost = 0.0M;
        string izraz = "";

        [DataMember]
        public decimal Vrednost { get; set; }

        [DataMember]
        public string Izraz { get; set; }
    }
}

namespace WcfKalkulator
{
    [ServiceBehaviour(InstanceContentMode = InstanceContentMode.Single)]
    public class Kalkulator : IKalkulator
    {
        private string nickname;
        private decimal vrednost;
        private string izraz;
        private Dictionary<string, IKalkulatorCallback> nicknameCallbacks;

        public Kalkulator()
        {
            nicknameCallbacks = new Dictionary<string, IKalkulatorCallback>();
        }

        public void setNicknameCallbacks(string nickname)
        {
            this.nickname = nickname;
            var c = OperationContext.Current.GetCallbackChannel<IKalkulatorCallback>();
            if(nicknameCallbacks.ContainsKey(nickname))
            {
                //vec ima taj nickname registrovan!
            }
            else 
            {
                nicknameCallbacks.Add(nickname, c);
            }
        }

        protected Dictionary<string, IKalkulatorCallback> Callback
        {
            get 
            {
                setNicknameCallbacks(nickname);
                return nicknameCallbacks;
            }
        }

        public void Dodaj(decimal d)
        {
            vrednost = vrednost + d;
            izraz = izraz + " + " + d.ToString();

            Callback.foreach(x=>x.VratiRezultat(new Rezultat(){
                Izraz = izraz, Vrednost = vrednost
            });
        }

        public void Register(string nickname)
        {
            this.nickname = nickname;
            setNicknameCallbacks(nickname);
        }
    }
}

namespace WcfKalkulator
{
    public interface IKalkulatorCallback 
    {
        [OperationContract(IsOneWay = true)]
        public void VratiRezultat(Rezultat r);
    }
}

namespace Klijent 
{
    public class Form1 : Form, IKalkulatorCallback
    {
        private KalkulatorClient proxy;

        public void VratiRezultat(Rezultat r)
        {
            textIzraz.Text = r.Izraz;
            textVrednost.Text = r.Vrednost.ToString();
        }

        public Form1()
        {
            InitializeComponent();
            proxy = new KalkulatorClient(new InstanceContext(this));
        }

        public void btnRegister_Click(object sender, EventArgs e)
        {
            string nickname = txtNickname.Text;
            proxy.Register(nickname);
        }

        public void btnPlus_Click(object sender, EventArgs e)
        {
            decimal broj;
            if(decimal.TryParse(txtBroj.Text, out broj))
            {
                proxy.Dodaj(broj);
            }
            else
            {
                MessageBox.Show("Pogresan unos broja!");
            }
        }

    }
}
```

```xml
<system.serviceModel>
    <services>
        <service name = "WcfKalkulator.Kalkulator">
            <endpoint 
                binding = "wsDualHttpBinding"
                address = "" 
                contract = "IKalkulator"
            />
        </service>
    </services>
</system.serviceModel>
```
