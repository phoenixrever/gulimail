package com.phoenixhell.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

//购物车
public class Cart {
    private List<CartItem> items;
    //商品总数量
    private Integer countNum;
    //商品有几种类型
    private Integer countType;
    //全部商品总价   amount 金额 数量 数额
    private BigDecimal totalAmount;
    //减免价格 reduce v 减少
    private BigDecimal reduce=BigDecimal.ZERO;

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        if (items != null && items.size() > 0) {
            //        int sum = items.stream().mapToInt(item -> item.getCount()).sum();
            Integer countNum = items.stream().map(item -> item.getCount()).reduce(0, (preValue, currentValue) -> preValue + currentValue);
            return countNum;
        }
        return 0;
    }

    public Integer getCountType() {
        return items.size();
    }

    public BigDecimal getTotalAmount() {
        if (items != null && items.size() > 0) {
            //        BigDecimal reduce = items.stream().map(item -> item.getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);
            //1计算总价格
            BigDecimal totalAmount = items.stream().filter(item->item.checked=true).map(item -> item.getTotalPrice()).reduce(BigDecimal.ZERO, (preValue, currentValue) -> preValue.add(currentValue));

            //2 减去减免优惠
            BigDecimal subtract = totalAmount.subtract(getReduce());
            return subtract;

        }
        return BigDecimal.ZERO;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }


    //购物项内容
    public static class CartItem {
        private Long skuId;
        private String title;
        private String defaultImg;
        private Boolean checked = true; //加入购物车就默认选中
        private List<String> skuAttr;
        private BigDecimal price;
        private Integer count;
        private BigDecimal totalPrice;

        public Long getSkuId() {
            return skuId;
        }

        public void setSkuId(Long skuId) {
            this.skuId = skuId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDefaultImg() {
            return defaultImg;
        }

        public void setDefaultImg(String defaultImg) {
            this.defaultImg = defaultImg;
        }

        public Boolean getChecked() {
            return checked;
        }

        public void setChecked(Boolean checked) {
            this.checked = checked;
        }

        public List<String> getSkuAttr() {
            return skuAttr;
        }

        public void setSkuAttr(List<String> skuAttr) {
            this.skuAttr = skuAttr;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        //BigDecimal 计算方法
        public BigDecimal getTotalPrice() {
            return price.multiply(BigDecimal.valueOf(count));
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}
