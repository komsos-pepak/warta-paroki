package komsos.wartaparoki.helper;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import komsos.wartaparoki.utils.Utils;

public class GenericOrSpesification<T> implements Specification<T> {

    private static final long serialVersionUID = 1900581010229669687L;

    private List<SearchCriteria> list;

    public GenericOrSpesification() {
        this.list = new ArrayList<>();
    }

    public void add(SearchCriteria criteria) {
        list.add(criteria);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        // create a new predicate list
        List<Predicate> predicates = new ArrayList<>();

        // add add criteria to predicates
        for (SearchCriteria criteria : list) {
            if (criteria.getOperation().equals(SearchOperation.EQUAL)) {
                if (criteria.getKey().contains("-")) {
                    String[] tamp = criteria.getKey().split("-");
                    switch (tamp.length) {
                        case 2:
                            predicates.add(builder.equal(
                                root.get(tamp[0]).get(tamp[1]), criteria.getValue()));
                            break;
                        case 3:
                            predicates.add(builder.equal(
                                root.get(tamp[0]).get(tamp[1]).get(tamp[2]), criteria.getValue()));
                            break;
                        case 4:
                            predicates.add(builder.equal(
                                root.get(tamp[0]).get(tamp[1]).get(tamp[2]).get(tamp[3]), criteria.getValue()));
                            break;
                        default:
                            break;
                    }
                } else {
                    predicates.add(builder.equal(
                        root.get(criteria.getKey()), criteria.getValue()));
                }
            } else if (criteria.getOperation().equals(SearchOperation.MATCH)) {
                if (criteria.getKey().contains("-")) {
                    String[] tamp = criteria.getKey().split("-");
                    switch (tamp.length) {
                        case 2:
                            predicates.add(builder.like(
                                builder.lower(root.get(tamp[0]).get(tamp[1])),
                                "%" + criteria.getValue().toString().toLowerCase() + "%"));
                            break;
                        case 3:
                            predicates.add(builder.like(
                                builder.lower(root.get(tamp[0]).get(tamp[1]).get(tamp[2])),
                                "%" + criteria.getValue().toString().toLowerCase() + "%"));
                            break;
                        case 4:
                            predicates.add(builder.like(
                                builder.lower(root.get(tamp[0]).get(tamp[1]).get(tamp[2]).get(tamp[3])),
                                "%" + criteria.getValue().toString().toLowerCase() + "%"));
                            break;
                        case 5:
                            predicates.add(builder.like(
                                builder.lower(root.get(tamp[0]).get(tamp[1]).get(tamp[2]).get(tamp[3]).get(tamp[4])),
                                "%" + criteria.getValue().toString().toLowerCase() + "%"));
                            break;
                        default:
                            break;
                    }
                } else {
                    predicates.add(builder.like(
                        builder.lower(root.get(criteria.getKey())),
                        "%" + criteria.getValue().toString().toLowerCase() + "%"));
                }
            } else if (criteria.getOperation().equals(SearchOperation.MATCH_END)) {
                if (criteria.getKey().contains("-")) {
                    String[] tamp = criteria.getKey().split("-");
                    switch (tamp.length) {
                        case 2:
                            predicates.add(builder.like(
                                builder.lower(root.get(tamp[0]).get(tamp[1])),
                                criteria.getValue().toString().toLowerCase() + "%"));
                            break;
                        case 3:
                            predicates.add(builder.like(
                                builder.lower(root.get(tamp[0]).get(tamp[1]).get(tamp[2])),
                                criteria.getValue().toString().toLowerCase() + "%"));
                            break;
                        case 4:
                            predicates.add(builder.like(
                                builder.lower(root.get(tamp[0]).get(tamp[1]).get(tamp[2]).get(tamp[3])),
                                criteria.getValue().toString().toLowerCase() + "%"));
                            break;
                        case 5:
                            predicates.add(builder.like(
                                builder.lower(root.get(tamp[0]).get(tamp[1]).get(tamp[2]).get(tamp[3]).get(tamp[4])),
                                criteria.getValue().toString().toLowerCase() + "%"));
                            break;
                        default:
                            break;
                    }
                } else {
                    predicates.add(builder.like(
                            builder.lower(root.get(criteria.getKey())),
                            criteria.getValue().toString().toLowerCase() + "%"));
                }
            } else if(criteria.getOperation().equals(SearchOperation.IN)){
                List<?> listUnitId= Utils.convertObjectToList(criteria.getValue());
                predicates.add(builder.in(
                    root.get(criteria.getKey())).value(listUnitId));
            } else if(criteria.getOperation().equals(SearchOperation.NOT_EMPTY)){
                predicates.add(builder.isNotEmpty(root.get(criteria.getKey())));
            } else if(criteria.getOperation().equals(SearchOperation.EMPTY)){
                predicates.add(builder.isEmpty(root.get(criteria.getKey())));
            }
        }
        return builder.or(predicates.toArray(new Predicate[0]));
    }
}