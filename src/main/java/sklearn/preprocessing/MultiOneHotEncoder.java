/*
 * Copyright (c) 2019 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package sklearn.preprocessing;

import java.util.ArrayList;
import java.util.List;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.OpType;
import org.jpmml.converter.BaseNFeature;
import org.jpmml.converter.BinaryFeature;
import org.jpmml.converter.CategoricalFeature;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.MissingValueFeature;
import org.jpmml.converter.ObjectFeature;
import org.jpmml.converter.TypeUtil;
import org.jpmml.converter.ValueUtil;
import org.jpmml.converter.WildcardFeature;
import org.jpmml.python.ClassDictUtil;
import org.jpmml.python.HasArray;
import org.jpmml.sklearn.SkLearnEncoder;
import sklearn.MultiTransformer;

public class MultiOneHotEncoder extends MultiTransformer {

	public MultiOneHotEncoder(String module, String name){
		super(module, name);
	}

	@Override
	public OpType getOpType(){
		return OpType.CATEGORICAL;
	}

	@Override
	public DataType getDataType(){
		return super.getDataType();
	}

	@Override
	public List<Feature> encodeFeatures(List<Feature> features, SkLearnEncoder encoder){
		List<List<?>> categories = getCategories();

		ClassDictUtil.checkSize(categories, features);

		Object drop = getDrop();
		List<Integer> dropIdx = (drop != null ? getDropIdx() : null);

		List<Feature> result = new ArrayList<>();

		for(int i = 0; i < features.size(); i++){
			Feature feature = features.get(i);
			List<Object> featureCategories = new ArrayList<>(categories.get(i));

			if(feature instanceof BaseNFeature){
				BaseNFeature baseFeature = (BaseNFeature)feature;

				ContinuousFeature continuousFeature = baseFeature.toContinuousFeature();

				// XXX
				encoder.toCategorical(continuousFeature.getName(), null);

				feature = continuousFeature;
			} else

			if(feature instanceof CategoricalFeature){
				CategoricalFeature categoricalFeature = (CategoricalFeature)feature;

				if(EncoderUtil.hasNaNCategory(featureCategories)){
					ClassDictUtil.checkSize(EncoderUtil.dropNaNCategory(featureCategories), categoricalFeature.getValues());

					featureCategories = new ArrayList<>(categoricalFeature.getValues());
					featureCategories.add(Double.NaN);
				} else

				{
					ClassDictUtil.checkSize(featureCategories, categoricalFeature.getValues());

					featureCategories = new ArrayList<>(categoricalFeature.getValues());
				}
			} else

			if(feature instanceof ObjectFeature){
				ObjectFeature objectFeature = (ObjectFeature)feature;
			} else

			if(feature instanceof WildcardFeature){
				WildcardFeature wildcardFeature = (WildcardFeature)feature;

				if(EncoderUtil.hasNaNCategory(featureCategories)){
					feature = wildcardFeature.toCategoricalFeature(EncoderUtil.dropNaNCategory(featureCategories));
				} else

				{
					feature = wildcardFeature.toCategoricalFeature(featureCategories);
				}

				CategoricalFeature categoricalFeature = (CategoricalFeature)feature;

				DataType dataType = TypeUtil.getDataType(categoricalFeature.getValues(), DataType.STRING);

				DataField dataField = (DataField)categoricalFeature.getField();
				if(!(dataField.getDataType()).equals(dataType)){
					dataField.setDataType(dataType);
				}
			} else

			{
				throw new IllegalArgumentException();
			} // End if

			if(dropIdx != null){
				// Unbox to primitive value in order to ensure correct List#remove(int) vs. List#remove(Object) method resolution
				int index = dropIdx.get(i);

				featureCategories.remove(index);
			}

			for(int j = 0; j < featureCategories.size(); j++){
				Object featureCategory = featureCategories.get(j);

				if(ValueUtil.isNaN(featureCategory)){
					result.add(new MissingValueFeature(encoder, feature));
				} else

				{
					result.add(new BinaryFeature(encoder, feature, featureCategory));
				}
			}
		}

		return result;
	}

	public List<List<?>> getCategories(){
		return EncoderUtil.transformCategories(getList("categories_", HasArray.class));
	}

	public Object getDrop(){
		return getOptionalObject("drop");
	}

	public List<Integer> getDropIdx(){
		return getIntegerArray("drop_idx_");
	}
}