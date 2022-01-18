Primer kada zelimo kalkulator koji korisnici dele medjusobno, kada jedan korisnik uradi operaciju, vidi je i drugi. Koristi se Single rezim. 

```csharp
namespace Wcfkalkulator
{
    [ServiceContract(CallbackContract = typeof(IKalkulatorCallback), SessionMode = SessionMode.Required)]
    public interface IKalkulator
    {
        [OperationContract(IsOneWay = true)]
        public void Dodaj(decimal d);

        [OperationContract(IsOneWay = true)]
        public void Oduzmi(decimal d);

        [OperationContract(IsoneWay = true)]
        public void Pomnozi(decimal d);

        [OperationContract(IsOneWay = true)]
        public void Podeli(decimal d);

        [OperationContract(IsOneWay = true)]
        public void Obrisi();
    }

    [DataContract]
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
    public interface IKalkulatorCallback   
    {
        [OperationContract(IsoneWay = true)]
        public void VratiRezultat(Rezultat r);
    }
}

namespace WcfKalkulator   
{
    [ServiceBehaviour(InstanceContentMode = InstanceContentMode.Single)]
    public class Kalkulator : IKalkulator  
    {
        private decimal vrednost;
        private string izraz;

        //private IKalkulatorCallback callback;

        //imamo property Callback koji je protected!
        protected IKalkulatorCallback Callback 
        {
            get
            {
                return OperationContext.Current.GetCallbackChannel<IKalkulatorCallback>();
            }
        }

        public Kalkulator()
        {
            vrednost = 0.0M;
            izraz = "";
        }

        public void Dodaj(decimal d)
        {
            this.vrednost += d;
            this.izraz += " + " + d.ToString();

            Callback.VratiRezultat(new Rezultat()
            {
                Vrednost = vrednost, Izraz = izraz
            });
        }

        public void Oduzmi(decimal d)
        {
            Dodaj(-d);
        }

        public void Pomnozi(decimal d)
        {
            this.vrednost *= d;
            this.izraz += " * " + d.ToString();

            Callback.VratiRezultat(new Rezultat()
            {
                Vrednost = vrednost, Izraz = izraz
            });
        }

        public void Podeli(decimal d)
        {
            if(d==0.0M)
            {
                this.vrednost = 0.0M;
                this.izraz = "";
                Callback.VratiRezultat(new Rezultat()
                {
                    Vrednost = vrednost, Izraz = izraz
                });
            }
            else
            {
                Pomnozi(1/d);
            }
        }

        public void Obrisi()
        {
            this.vrednost = 0.0M;
            this.izraz = "";

            Callback.VratiRezultat(new Rezultat()
            {
                Vrednost = vrednost, Izraz = izraz
            });
        }

    }
}

namespace WcfKalkulator.Client
{
    public class Form1 : Form, IKalkulatorCallback
    {
        private KalkulatorClient proxy;

        public Form1()
        {
            InitializeComponent();
            proxy = new KalkulatorClient(new InstanceContext(this));
        }

        public void VratiRezultat(Rezultat r)
        {
            lblIzraz.Text = r.Izraz;
            lblVrednost.Text = r.Vrednost.ToString();
        }

        public void btnOperate_Click(object sender, EventArgs e)
        {
            //...
        }

        public void btnObrisi_Click(object sender, EventArgs e)
        {
            //...
        }
    }
}
```
Web.config
```html
<system.serviceModel>
    <services>
        <service name = "WcfKalkulator.Kalkulator">
            <endpoint 
            binding = "wsDualHttpBinding"
            contract = "WcfKalkulator.IKalkulator"
            />
        <service/>
    </services>
</system.serviceModel>
```
