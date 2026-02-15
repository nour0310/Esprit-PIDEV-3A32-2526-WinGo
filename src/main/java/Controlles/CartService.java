package Controlles;

import Entites.Produit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CartService {

    private final Map<Integer, CartItem> map = new LinkedHashMap<>();

    public void add(Produit p, int qty) {
        if (p == null || qty <= 0) return;

        int id = p.getIdProduit();
        CartItem it = map.get(id);

        String img = (p.getImage() == null) ? "" : p.getImage();

        if (it == null) {
            // ✅ constructeur 5 params (id, nom, image, prix, qty)
            map.put(id, new CartItem(id, p.getNom(), img, p.getPrix(), qty));
        } else {
            it.setQty(it.getQty() + qty);
        }
    }

    public void changeQty(int idProduit, int delta) {
        CartItem it = map.get(idProduit);
        if (it == null) return;

        int newQty = it.getQty() + delta;
        if (newQty <= 0) map.remove(idProduit);
        else it.setQty(newQty);
    }

    public void remove(int idProduit) {
        map.remove(idProduit);
    }

    public void clear() {
        map.clear();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(map.values());
    }

    public int totalQty() {
        int sum = 0;
        for (CartItem it : map.values()) sum += it.getQty();
        return sum;
    }

    public double totalPrice() {
        double sum = 0;
        for (CartItem it : map.values()) sum += it.getSubtotal();
        return sum;
    }
}