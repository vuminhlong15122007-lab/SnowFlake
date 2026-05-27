package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.domain.Item;

public abstract class ItemFactory {
  public abstract void showData();

  public abstract Item createItemFromForm();
}
