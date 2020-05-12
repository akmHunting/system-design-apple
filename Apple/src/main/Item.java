package main;
import java.util.ArrayList;
import java.util.List;

public class Item {
	
        protected String itemName;
        protected  List<Item> dependencyList;

        public Item(String moduleName) {
            this.itemName = moduleName;
            dependencyList = new ArrayList<>();
        }
  };