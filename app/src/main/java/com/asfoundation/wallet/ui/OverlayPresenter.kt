package com.asfoundation.wallet.ui

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class OverlayPresenter(private val view: OverlayView, private val disposable: CompositeDisposable) {

  fun present() {
    handleDismissClick()
    handleDiscoverClick()
  }

  private fun handleDiscoverClick() {
    disposable.add(view.discoverClick()
        .doOnNext { view.navigateToPromotions() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleDismissClick() {
    disposable.add(Observable.merge(view.dismissClick(), view.overlayClick())
        .doOnNext { view.dismissView() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }
}
