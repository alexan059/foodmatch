package com.fancyfood.foodmatch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.models.Card;

import java.util.List;

/**
 * Connects Card Model Lists with card item layout.
 * Note: Efficient programming style to recycle views and decrease memory usage as getView()
 * using a view holder. (Android Programming Conventions)
 */
public class CardAdapter extends ArrayAdapter<Card> {

    public CardAdapter(Context context, int resource) {
        super(context, resource);
    }

    public CardAdapter(Context context, int resource, List<Card> items) {
        super(context, resource, items);
    }

    /**
     * Holds values like enums.
     */
    static class ViewHolder {
        ImageView image;
        TextView dish;
        TextView location;
        TextView distance;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Define a view holder
        ViewHolder viewHolder;

        // View wasn't defined before
        if (convertView == null) {
            // Get the layout inflater
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(getContext());

            // Insert layout into main layout
            // Note: In this case we insert a new card into the views parent, since it's not
            // the root of our layout the third parameter has to be false.
            convertView = layoutInflater.inflate(R.layout.card_item, parent, false);

            // Assign views to view holder
            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) convertView.findViewById(R.id.ivDish);
            viewHolder.dish = (TextView) convertView.findViewById(R.id.tvDish);
            viewHolder.location = (TextView) convertView.findViewById(R.id.tvLocation);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.tvDistance);

            // Store view
            convertView.setTag(viewHolder);
        } else {
            // Retrieve view if convertView not null
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Values
        String distance = Double.toString(getItem(position).getDistance()) + "m";

        // Assign new data to view holder
        viewHolder.image.setImageDrawable(getItem(position).getImage());
        viewHolder.dish.setText(getItem(position).getDish());
        viewHolder.location.setText(getItem(position).getLocationName());
        viewHolder.distance.setText(distance);


        // Return manipulated view
        return convertView;
    }
}
