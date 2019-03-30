package base.dto.request;

import base.model.Identifiable;

import java.util.List;

/**
 * @author csieflyman
 */
public class BatchFormRequest<ID, T extends Identifiable<ID>> extends FormRequest<List<T>> {

}
