package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.poa.CountryCodeProvider;
import com.asfoundation.wallet.poa.DataMapper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by trinkes on 3/16/18.
 */
@RunWith(MockitoJUnitRunner.class) public class BuyServiceTest {

  public static final String PACKAGE_NAME = "package_name";
  public static final String PRODUCT_NAME = "product_name";
  @Mock SendTransactionInteract sendTransactionInteract;
  @Mock TrackTransactionService trackTransactionService;

  @Mock TransactionSender transactionSender;
  @Mock TransactionValidator transactionValidator;
  @Mock DefaultTokenProvider defaultTokenProvider;
  private TestScheduler scheduler;
  private WatchedTransactionService transactionService;
  private TransactionBuilder transactionBuilder;
  @Mock CountryCodeProvider countryCodeProvider;
  private DataMapper dataMapper;

  @Before public void setup() {

    scheduler = new TestScheduler();
    transactionService = new WatchedTransactionService(transactionSender,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), new ErrorMapper(),
        scheduler, trackTransactionService);
    when(transactionValidator.validate(any())).thenReturn(Completable.complete());
    TokenInfo tokenInfo =
        new TokenInfo("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", "Appcoins", "APPC", 18, false,
            false);
    transactionBuilder =
        new TransactionBuilder("APPC", "0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", 3l,
            "0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", BigDecimal.ONE, "sku", 18,
            "0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3");
    when(transactionSender.send(transactionBuilder)).thenReturn(Single.just("hash"));
    when(defaultTokenProvider.getDefaultToken()).thenReturn(Single.just(tokenInfo));
    when(countryCodeProvider.getCountryCode()).thenReturn(Single.just("PT"));
    dataMapper = new DataMapper();
  }

  @Test public void buy() {
    PublishSubject<PendingTransaction> pendingTransactionState = PublishSubject.create();
    when(trackTransactionService.checkTransactionState(anyString())).thenReturn(
        pendingTransactionState);

    BuyService buyService =
        new BuyService(transactionService, transactionValidator, defaultTokenProvider,
            countryCodeProvider, dataMapper);
    buyService.start();
    scheduler.triggerActions();

    String uri = "uri";
    TestObserver<BuyService.BuyTransaction> observer = new TestObserver<>();
    buyService.getBuy(uri)
        .subscribe(observer);

    buyService.buy(uri,
        new PaymentTransaction(uri, transactionBuilder, PaymentTransaction.PaymentState.APPROVED,
            "", null, PACKAGE_NAME, PRODUCT_NAME))
        .subscribe();

    scheduler.triggerActions();
    pendingTransactionState.onNext(new PendingTransaction("hash", true));
    scheduler.triggerActions();
    pendingTransactionState.onNext(new PendingTransaction("hash", false));
    pendingTransactionState.onComplete();
    scheduler.triggerActions();
    List<BuyService.BuyTransaction> values = observer.values();
    Assert.assertEquals(values.size(), 3);
    Assert.assertEquals(values.get(2)
        .getStatus(), BuyService.Status.BOUGHT);
  }

  @Test public void buyTransactionNotFound() {
    Observable<PendingTransaction> pendingTransactionState =
        Observable.just(new PendingTransaction("hash", true),
            new PendingTransaction("hash", false));
    when(trackTransactionService.checkTransactionState("hash")).thenReturn(pendingTransactionState);

    BuyService buyService =
        new BuyService(transactionService, transactionValidator, defaultTokenProvider,
            countryCodeProvider, dataMapper);
    buyService.start();

    String uri = "uri";
    TestObserver<BuyService.BuyTransaction> observer = new TestObserver<>();
    buyService.getBuy(uri)
        .subscribe(observer);
    scheduler.triggerActions();
    buyService.buy(uri,
        new PaymentTransaction(uri, transactionBuilder, PaymentTransaction.PaymentState.APPROVED,
            "", null, PACKAGE_NAME, PRODUCT_NAME))
        .subscribe();
    scheduler.triggerActions();

    List<BuyService.BuyTransaction> values = observer.values();
    Assert.assertEquals(3, values.size());
    Assert.assertEquals(BuyService.Status.BOUGHT, values.get(2)
        .getStatus());
  }
}