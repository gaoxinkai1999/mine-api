package com.example.modules.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Entity
@Table(name = "price_rule", schema = "mine")
public class PriceRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;


    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "is_die", nullable = false)
    private boolean isDie;

    @ToString.Exclude
    @OneToMany(mappedBy = "priceRule")
    private List<PriceRuleDetail> priceRuleDetails;
    @Column(name = "color", length = 20)
    private String color;


    // 添加规则项
    public void addRuleItem(PriceRuleDetail item) {
        priceRuleDetails.add(item);
        item.setPriceRule(this);
    }

    // 移除规则项
    public void removeRuleItem(PriceRuleDetail item) {
        priceRuleDetails.remove(item);
        item.setPriceRule(null);
    }

}