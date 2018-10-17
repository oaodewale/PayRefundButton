package demo.payworks.io.payrefundbutton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.EnumSet;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.provider.ProviderMode;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.parameters.TransactionParameters;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;





public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button payButton = (Button) findViewById(R.id.pay);
        payButton.setOnClickListener(new View.OnClickListener()    {
            @Override
            public void onClick(View v) {
                paymentButtonClicked();
            }
        });
    }

    void paymentButtonClicked() {
        MposUi ui = MposUi.initialize(this, ProviderMode.MOCK,
                "merchantIdentifier", "merchantSecretKey");

        ui.getConfiguration().setSummaryFeatures(EnumSet.of(
                // Add this line, if you do want to offer printed receipts
                // MposUiConfiguration.SummaryFeature.PRINT_RECEIPT,
                MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL)
        );

        // Start with a mocked card reader:
        AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MOCK)
                .mocked()
                .build();
        ui.getConfiguration().setTerminalParameters(accessoryParameters);


        TransactionParameters transactionParameters = new TransactionParameters.Builder()
                .charge(new BigDecimal("5.00"), io.mpos.transactions.Currency.EUR)
                .subject("Bouquet of Flowers")
                .customIdentifier("yourReferenceForTheTransaction")
                .build();

        Intent intent = ui.createTransactionIntent(transactionParameters);
        startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);
    }

    void refundButtonClicked() {
        TransactionParameters parameters = new TransactionParameters.Builder()
                .refund("yourReferenceForTheTransaction")
                .subject("Bouquet of Flowers")
                .amountAndCurrency(new BigDecimal(
                                ((EditText) (findViewById(R.id.money))).getText().toString()),
                        io.mpos.transactions.Currency.EUR)
                .build();
        Intent intent = MposUi.getInitializedInstance().createTransactionIntent(parameters);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MposUi.REQUEST_CODE_PAYMENT) {
            if (resultCode == MposUi.RESULT_CODE_APPROVED) {
                // Transaction was approved
                Toast.makeText(this, "Transaction approved", Toast.LENGTH_LONG).show();

                findViewById(R.id.refund).setVisibility(View.VISIBLE);
                findViewById(R.id.pay).setVisibility(View.GONE);
                findViewById(R.id.money).setVisibility(View.GONE);
                findViewById(R.id.AMOUNT).setVisibility(View.GONE);
            }

            } else {
                // Card was declined, or transaction was aborted, or failed
                // (e.g. no internet or accessory not found)
                Toast.makeText(this, "Transaction was declined, aborted, or failed",
                        Toast.LENGTH_LONG).show();


                findViewById(R.id.refund).setVisibility(View.GONE);
                findViewById(R.id.pay).setVisibility(View.VISIBLE);
                findViewById(R.id.money).setVisibility(View.VISIBLE);
                findViewById(R.id.AMOUNT).setVisibility(View.VISIBLE);

            }
            // Grab the processed transaction in case you need it
            // (e.g. the transaction identifier for a refund).
            // Keep in mind that the returned transaction might be null
            // (e.g. if it could not be registered).
            Transaction transaction = MposUi.getInitializedInstance().getTransaction();
        }
    }
}