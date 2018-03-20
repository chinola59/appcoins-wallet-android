package com.asfoundation.wallet.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.MyTokensRouter;

public class AddTokenViewModel extends BaseViewModel {

  private final AddTokenInteract addTokenInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final MyTokensRouter myTokensRouter;

  private final MutableLiveData<Boolean> result = new MutableLiveData<>();

  AddTokenViewModel(AddTokenInteract addTokenInteract,
      FindDefaultWalletInteract findDefaultWalletInteract, MyTokensRouter myTokensRouter) {
    this.addTokenInteract = addTokenInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.myTokensRouter = myTokensRouter;
  }

  public void save(String address, String symbol, int decimals) {
    addTokenInteract.add(address, symbol, decimals)
        .subscribe(this::onSaved, this::onError);
  }

  private void onSaved() {
    progress.postValue(false);
    result.postValue(true);
  }

  public LiveData<Boolean> result() {
    return result;
  }

  public void showTokens(Context context) {
    findDefaultWalletInteract.find()
        .subscribe(w -> myTokensRouter.open(context, w), this::onError);
  }
}