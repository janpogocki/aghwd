package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jan on 04.08.2016.
 * Store password in secure storage
 */

public class RememberPassword {
    private AccountManager accountManager;
    private Account myAccount = null;
    private Context context;

    public RememberPassword(Context c){
        context = c;
        accountManager = AccountManager.get(context);
        getAccount();
    }

    private void getAccount(){
        Account[] accounts = accountManager.getAccountsByType("pl.janpogocki.agh.wirtualnydziekanat");
        for (Account account : accounts) {
            if (account.name.length() >= 6) {
                myAccount = accounts[0];
                break;
            }
        }
    }

    public boolean isRemembered(){
        return myAccount != null;
    }

    public boolean hasExtraData(){
        try {
            if (getPeselNumber() != null && getNameAndSurname() != null)
                return true;
        } catch (IOException | IllegalArgumentException e) {
            Log.i("aghwd", "No AGHWD account", e);
        }

        return false;
    }

    public void save(String _login, String _password, String _pesel, String _nameAndSurname) {
        Account account = new Account(_login, "pl.janpogocki.agh.wirtualnydziekanat");
        Bundle extraData = new Bundle();
        extraData.putString("peselNumber", _pesel);
        extraData.putString("nameAndSurname", _nameAndSurname);
        accountManager.addAccountExplicitly(account, _password, extraData);
    }

    public void remove(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccount(myAccount, null, null, null);
        } else {
            accountManager.removeAccount(myAccount, null, null);
        }

        // delete cached photo
        File file = new File(context.getFilesDir() + "/" + Logging.photoFileName);
        file.delete();
    }

    public String getLogin() throws IOException {
        return myAccount.name;
    }

    public String getPassword() throws IOException {
        return accountManager.getPassword(myAccount);
    }

    public String getPeselNumber() throws IOException {
        return accountManager.getUserData(myAccount, "peselNumber");
    }

    public String getNameAndSurname() throws IOException {
        return accountManager.getUserData(myAccount, "nameAndSurname");
    }
}
