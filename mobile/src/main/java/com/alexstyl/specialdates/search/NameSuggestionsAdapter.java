package com.alexstyl.specialdates.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.alexstyl.specialdates.SoundWordComparator;
import com.alexstyl.specialdates.WordComparator;
import com.alexstyl.specialdates.date.Date;
import com.alexstyl.specialdates.events.namedays.calendar.NamedayCalendar;
import com.alexstyl.specialdates.events.namedays.calendar.resource.NamedayCalendarProvider;
import com.alexstyl.specialdates.events.namedays.NamedayLocale;
import com.alexstyl.specialdates.events.namedays.NamedayPreferences;

import java.util.ArrayList;
import java.util.List;

class NameSuggestionsAdapter extends RecyclerView.Adapter<NameViewHolder> implements Filterable {

    private OnNameSelectedListener listener;
    private final Filter filter;
    private final List<String> displayingNames;

    public static NameSuggestionsAdapter newInstance(Context context) {
        NamedayPreferences namedayPreferences = NamedayPreferences.newInstance(context);
        NamedayCalendarProvider namedayCalendarProvider = NamedayCalendarProvider.newInstance(context.getResources());
        NamedayLocale locale = namedayPreferences.getSelectedLanguage();
        WordComparator compatator;
        if (locale.isComparedBySound()) {
            compatator = new SoundWordComparator();
        } else {
            compatator = new CaseInsensitiveComparator();
        }
        int year = Date.today().getYear();
        NamedayCalendar namedayCalendar = namedayCalendarProvider.loadNamedayCalendarForLocale(locale, year);
        List<String> names = namedayCalendar.getAllNames();
        return new NameSuggestionsAdapter(new NameFilter(names, compatator));
    }

    private NameSuggestionsAdapter(NameFilter nameFilter) {
        this.displayingNames = new ArrayList<>();
        this.filter = createFilterFor(nameFilter);
    }

    @Override
    public NameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return NameViewHolder.createFor(parent);
    }

    @Override
    public void onBindViewHolder(NameViewHolder holder, int position) {
        holder.bind(displayingNames.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return displayingNames.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @NonNull
    private Filter createFilterFor(final NameFilter nameFilter) {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<String> names = nameFilter.performFiltering(constraint);
                FilterResults filterResults = new FilterResults();
                filterResults.count = names.size();
                filterResults.values = names;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence typedName, FilterResults results) {
                displayingNames.clear();
                ArrayList<String> names = (ArrayList<String>) results.values;
                displayingNames.addAll(names);
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return resultValue.toString();
            }
        };
    }

    public void setOnNameSelectedListener(OnNameSelectedListener l) {
        this.listener = l;
    }

    public void clearNames() {
        this.displayingNames.clear();
        notifyDataSetChanged();
    }

    public interface OnNameSelectedListener {

        void onNameSelected(View view, String name);

    }

}
