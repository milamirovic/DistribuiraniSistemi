```xml
<system.serviceModel>
    <services>
        <service name = "WcfCalculator.Calculator">
            <endpoint
            binding = "wsDualHttpBinding"
            contract = "WcfCalculator.ICalculator"
            address = ""
            />
        </service>
    </services>
</system.serviceModel>
```
```csharp
namespace WcfCalculator 
{
    [ServiceContract(CallbacksContract = typeof(ICalculatorCallback), SessionMode = SessionMode.Required)]
    public interface ICalculator 
    {
        [OperationContract(IsOneWay=true)]
        public void Dodaj(decimal d);

        [OperationContract(IsOneWay=true)]
        public void Oduzmi(decimal d);

        [OperationContract(IsOneWay=true)]
        public void Pomnozi(decimal d);

        [OperationContract(IsOneWay=true)]
        public void Podeli(decimal d);

        [OperationContract(IsOneWay=true)]
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

namespace WcfCalculator
{
    public interface ICalculatorCallback 
    {
        [OperationContract(IsOneWay=true)]
        public void VratiRezultat(Rezultat r);
    }
}

namespace WcfCalculator
{
    [ServiceBehaviour(InstanceContentMode = InstanceContentMode.PerSession)]
    public class Calculator : ICalculator
    {
        private decimal r = 0.0M;
        private string izraz = "";
        private ICalculatorCallback callback;

        public Calculator()
        {
            callback = OperationContext.Current.GetCallbackChannel<ICalcCallback>();
        }

        protected ICalaculatorCallback Callback
        {
            get
            {
                return OperationContext.Current.GetCallbackChannel<ICalculatorCallback>();
            }
        }

        public void Dodaj(decimal d)
        {
            r = r + d;
            izraz = izraz + $" + {d}";

            callback.VratiRezultat(new Rezultat()
            {
                Vrednost = r,
                Izraz = izraz
            });
        }

        public void Oduzmi(decimal d)
        {
            r = r - d;
            izraz = izraz + $" - {d}";

            callback.VratiRezultat(new Rezultat()
            {
                Vrednost = r,
                Izraz = izraz
            });
        }

        public void Pomnozi(decimal d)
        {
            r = r * d;
            izraz = izraz + $" * {d}";

            callback.VratiRezultat(new Rezultat()
            {
                Vrednost = r,
                Izraz = izraz
            });
        }

        public void Podeli(decimal d)
        {
            if(d == 0.0M)
            {
                r = 0.0M;
                izraz = "Deljenje nulom nije moguce!";
            }
            else 
            {
                r = r / d;
                izraz = izraz + $" / {d}";
            }

            callback.VratiRezultat(new Rezultat()
            {
                Vrednost = r,
                Izraz = izraz
            });
        }

        public void Obrisi()
        {
            r = 0.0M;
            izraz = "";
            callback.VratiRezultat(new Rezultat()
            {
                Vrednost = r, 
                Izraz = izraz
            });
        }
    }
}

namspace CalculatorKlijent 
{
    public class Form1 : Form, ICalaculatorCallback
    {
        private CalculcatorClient proxy;

        public Form1()
        {
            InitializeComponent();

            proxy = new CalculatorClient(new InstanceContext(this));
        }

        //implementira metod interfejsa ICalculatorCallback
        public void VratiRezultat(Rezultat r)
        {
            txtRezultat.Text = r.Vrednost.ToString();
            txtIzraz.Text = r.Izraz;
        }

        private void btnPlus_Click(object sender, EventArgs e)
        {
            decimal d;
            if(decimal.TryParse(txtUnos.Text, out d))
            {
                proxy.Dodaj(d);
            }
            else 
            {
                MessageBox.Show("greska u prevodjenju broja!");
            }
        }

        private btnMinus_Click(object sender, EventArgs e)
        {
            decimal d;
            if(decimal.TryParse(txtUnos.Text, out d))
            {
                proxy.Oduzmi(d);
            }
            else 
            {
                MessageBox.Show("greska u prevodjenju broja!");
            }
        }

        private btnPuta_Click(object sender, EventArgs e)
        {
            decimal d;
            if(decimal.TryParse(txtUnos.Text, out d))
            {
                proxy.Pomnozi(d);
            }
            else 
            {
                MessageBox.Show("greska u prevodjenju broja!");
            }
        }

        private btnPodeljeno_Click(object sender, Eventargs e)
        {
            decimal d;
            if(decimal.TryParse(txtUnos.Text, out d))
            {
                proxy.Podeli(d);
            }
            else 
            {
                MessageBox.Show("greska u prevodjenju broja!");
            }
        }

        private btnObrisi_Click(object sender, EventArgs e)
        {
            proxy.Obrisi();
        }
    }
}
```
